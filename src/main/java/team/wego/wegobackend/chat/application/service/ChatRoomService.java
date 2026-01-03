package team.wego.wegobackend.chat.application.service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.wego.wegobackend.chat.application.dto.response.ChatRoomItemResponse;
import team.wego.wegobackend.chat.application.dto.response.ChatRoomListResponse;
import team.wego.wegobackend.chat.application.dto.response.ChatRoomResponse;
import team.wego.wegobackend.chat.application.dto.response.LastMessageResponse;
import team.wego.wegobackend.chat.application.dto.response.ParticipantListResponse;
import team.wego.wegobackend.chat.application.dto.response.ParticipantResponse;
import team.wego.wegobackend.chat.application.dto.response.ReadStatusResponse;
import team.wego.wegobackend.chat.domain.entity.ChatMessage;
import team.wego.wegobackend.chat.domain.entity.ChatParticipant;
import team.wego.wegobackend.chat.domain.entity.ChatRoom;
import team.wego.wegobackend.chat.domain.entity.ChatType;
import team.wego.wegobackend.chat.domain.entity.JoinType;
import team.wego.wegobackend.chat.domain.entity.ParticipantStatus;
import team.wego.wegobackend.chat.domain.exception.ChatErrorCode;
import team.wego.wegobackend.chat.domain.exception.ChatException;
import team.wego.wegobackend.chat.domain.repository.ChatMessageRepository;
import team.wego.wegobackend.chat.domain.repository.ChatParticipantRepository;
import team.wego.wegobackend.chat.domain.repository.ChatRoomRepository;
import team.wego.wegobackend.group.v2.domain.entity.GroupImageV2;
import team.wego.wegobackend.group.v2.domain.entity.GroupImageV2VariantType;
import team.wego.wegobackend.group.v2.domain.entity.GroupV2;
import team.wego.wegobackend.group.v2.domain.repository.GroupImageV2Repository;
import team.wego.wegobackend.group.v2.domain.repository.GroupV2Repository;
import team.wego.wegobackend.user.domain.User;
import team.wego.wegobackend.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final GroupV2Repository groupV2Repository;
    private final GroupImageV2Repository groupImageV2Repository;

    /**
     * 내 채팅방 목록 조회
     */
    public ChatRoomListResponse getMyChatRooms(Long userId) {
        List<ChatRoom> chatRooms = chatRoomRepository.findAllByUserIdAndActiveStatus(userId);

        List<ChatRoomItemResponse> items = chatRooms.stream()
                .map(chatRoom -> buildChatRoomItem(chatRoom, userId))
                .sorted(Comparator.comparing(
                    item -> item.lastMessage() != null ? item.lastMessage().timestamp() : null, //Group 채팅의 경우 lastMessage가 비어있는 경우 존재 -> NPE 처리
                    Comparator.nullsLast(Comparator.reverseOrder())  // null 처리 + 최신순
                ))
                .collect(Collectors.toList());

        return ChatRoomListResponse.from(items);
    }

    /**
     * 채팅방 상세 조회
     */
    public ChatRoomResponse getChatRoom(Long userId, Long roomId) {
        ChatRoom chatRoom = findChatRoomById(roomId);
        validateParticipant(chatRoom.getId(), userId);

        Long hostId = chatRoom.getHostId();
        List<ParticipantResponse> participants = chatParticipantRepository
                .findActiveParticipants(roomId)
                .stream()
                .map(p -> ParticipantResponse.from(p, isOwner(p, hostId)))
                .collect(Collectors.toList());

        String chatRoomName = resolveChatRoomName(chatRoom, userId);
        String thumbnail = resolveThumbnail(chatRoom, userId);

        return ChatRoomResponse.of(chatRoom, chatRoomName, thumbnail, participants);
    }

    /**
     * 채팅방 참여자 목록 조회
     */
    public ParticipantListResponse getParticipants(Long userId, Long roomId) {
        ChatRoom chatRoom = findChatRoomById(roomId);
        validateParticipant(chatRoom.getId(), userId);

        Long hostId = chatRoom.getHostId();
        List<ParticipantResponse> participants = chatParticipantRepository
                .findActiveParticipants(roomId)
                .stream()
                .map(p -> ParticipantResponse.from(p, isOwner(p, hostId)))
                .collect(Collectors.toList());

        return ParticipantListResponse.of(roomId, participants);
    }

    /**
     * 읽음 처리
     */
    @Transactional
    public ReadStatusResponse markAsRead(Long userId, Long roomId) {
        ChatParticipant participant = findParticipant(roomId, userId);

        ChatMessage latestMessage = chatMessageRepository.findLatestByChatRoomId(roomId)
                .orElse(null);

        Long lastReadMessageId = latestMessage != null ? latestMessage.getId() : null;

        if (lastReadMessageId != null) {
            participant.updateLastReadMessageId(lastReadMessageId);
        }

        return ReadStatusResponse.of(roomId, lastReadMessageId, 0);
    }

    /**
     * 참여자 추방 (방장 전용)
     */
    @Transactional
    public void kickParticipant(Long userId, Long roomId, Long targetUserId) {
        ChatRoom chatRoom = findChatRoomById(roomId);

        // 자기 자신 추방 방지
        if (userId.equals(targetUserId)) {
            throw new ChatException(ChatErrorCode.CANNOT_KICK_SELF);
        }

        // 그룹 채팅방인 경우 방장 권한 확인
        if (chatRoom.isGroupChat()) {
            validateHost(chatRoom, userId);
        }

        ChatParticipant targetParticipant = findParticipant(roomId, targetUserId);
        targetParticipant.kick();

        log.info("참여자 추방 - roomId: {}, targetUserId: {}, kickedBy: {}", roomId, targetUserId, userId);
    }

    /**
     * 채팅방 나가기
     */
    @Transactional
    public void leaveChatRoom(Long userId, Long roomId) {
        ChatParticipant participant = findParticipant(roomId, userId);
        participant.leave();

        log.info("채팅방 퇴장 - roomId: {}, userId: {}", roomId, userId);
    }

    /**
     * 1:1 채팅방 생성 또는 조회
     */
    @Transactional
    public ChatRoomResponse createOrGetDmRoom(Long userId, Long targetUserId) {
        // 자기 자신에게 DM 방지
        if (userId.equals(targetUserId)) {
            throw new ChatException(ChatErrorCode.CANNOT_DM_SELF);
        }

        User user = findUserById(userId);
        User targetUser = findUserById(targetUserId);

        // 기존 DM 채팅방 조회
        return chatRoomRepository.findDmChatRoom(userId, targetUserId)
                .map(chatRoom -> buildChatRoomResponse(chatRoom, userId))
                .orElseGet(() -> createDmRoom(user, targetUser));
    }

    /**
     * 모임용 그룹 채팅방 생성 (이벤트 리스너에서 호출)
     */
    @Transactional
    public ChatRoom createGroupChatRoomForMeeting(Long groupId, Long hostUserId) {
        GroupV2 group = groupV2Repository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("그룹을 찾을 수 없습니다: " + groupId));

        User host = findUserById(hostUserId);

        // 이미 채팅방이 있는지 확인
        if (chatRoomRepository.findByGroupId(groupId).isPresent()) {
            log.warn("이미 그룹 채팅방이 존재합니다 - groupId: {}", groupId);
            return chatRoomRepository.findByGroupId(groupId).get();
        }

        ChatRoom chatRoom = ChatRoom.createGroupChat(group);
        chatRoomRepository.save(chatRoom);

        // 방장을 참여자로 추가
        ChatParticipant hostParticipant = ChatParticipant.create(host, JoinType.AUTO);
        chatRoom.addParticipant(hostParticipant);

        log.info("그룹 채팅방 생성 - groupId: {}, chatRoomId: {}", groupId, chatRoom.getId());

        return chatRoom;
    }

    /**
     * 모임 참여 시 채팅방 자동 참여 (이벤트 리스너에서 호출)
     */
    @Transactional
    public void joinChatRoomByGroup(Long groupId, Long userId, JoinType joinType) {
        ChatRoom chatRoom = chatRoomRepository.findByGroupId(groupId)
                .orElseThrow(() -> new ChatException(ChatErrorCode.CHAT_ROOM_NOT_FOUND));

        User user = findUserById(userId);

        // 이미 참여 중인지 확인
        chatParticipantRepository.findByChatRoomIdAndUserId(chatRoom.getId(), userId)
                .ifPresentOrElse(
                        participant -> {
                            if (!participant.isActive()) {
                                participant.rejoin(joinType);
                                log.info("채팅방 재참여 - roomId: {}, userId: {}", chatRoom.getId(), userId);
                            }
                        },
                        () -> {
                            ChatParticipant newParticipant = ChatParticipant.create(user, joinType);
                            chatRoom.addParticipant(newParticipant);
                            log.info("채팅방 참여 - roomId: {}, userId: {}", chatRoom.getId(), userId);
                        }
                );
    }

    /**
     * 모임 퇴장 시 채팅방 퇴장 (이벤트 리스너에서 호출)
     */
    @Transactional
    public void leaveChatRoomByGroup(Long groupId, Long userId) {
        chatRoomRepository.findByGroupId(groupId)
                .ifPresent(chatRoom -> {
                    chatParticipantRepository.findByChatRoomIdAndUserId(chatRoom.getId(), userId)
                            .ifPresent(ChatParticipant::leave);
                    log.info("채팅방 퇴장 (모임 연동) - groupId: {}, userId: {}", groupId, userId);
                });
    }

    // ===== Private Helper Methods =====

    private ChatRoom findChatRoomById(Long roomId) {
        return chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ChatException(ChatErrorCode.CHAT_ROOM_NOT_FOUND));
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));
    }

    private ChatParticipant findParticipant(Long roomId, Long userId) {
        return chatParticipantRepository.findByChatRoomIdAndUserId(roomId, userId)
                .filter(ChatParticipant::isActive)
                .orElseThrow(() -> new ChatException(ChatErrorCode.NOT_CHAT_PARTICIPANT));
    }

    private void validateParticipant(Long roomId, Long userId) {
        boolean isParticipant = chatParticipantRepository
                .existsByChatRoomIdAndUserIdAndStatus(roomId, userId, ParticipantStatus.ACTIVE);

        if (!isParticipant) {
            throw new ChatException(ChatErrorCode.NOT_CHAT_PARTICIPANT);
        }
    }

    private void validateHost(ChatRoom chatRoom, Long userId) {
        if (chatRoom.getGroup() == null) {
            return;
        }

        Long hostId = chatRoom.getGroup().getHost().getId();
        if (!hostId.equals(userId)) {
            throw new ChatException(ChatErrorCode.NOT_CHAT_ROOM_OWNER);
        }
    }

    private ChatRoomItemResponse buildChatRoomItem(ChatRoom chatRoom, Long userId) {
        String chatRoomName = resolveChatRoomName(chatRoom, userId);
        String thumbnail = resolveThumbnail(chatRoom, userId);
        int participantCount = chatParticipantRepository.countActiveParticipants(chatRoom.getId());

        LastMessageResponse lastMessage = chatMessageRepository.findLatestByChatRoomId(chatRoom.getId())
                .map(msg -> LastMessageResponse.from(
                        msg,
                        msg.getSender() != null ? msg.getSender().getNickName() : null
                ))
                .orElse(null);

        int unreadCount = calculateUnreadCount(chatRoom.getId(), userId);

        return ChatRoomItemResponse.of(chatRoom, chatRoomName, thumbnail, participantCount, lastMessage, unreadCount);
    }

    private ChatRoomResponse buildChatRoomResponse(ChatRoom chatRoom, Long userId) {
        String chatRoomName = resolveChatRoomName(chatRoom, userId);
        String thumbnail = resolveThumbnail(chatRoom, userId);

        Long hostId = chatRoom.getHostId();
        List<ParticipantResponse> participants = chatParticipantRepository
                .findActiveParticipants(chatRoom.getId())
                .stream()
                .map(p -> ParticipantResponse.from(p, isOwner(p, hostId)))
                .collect(Collectors.toList());

        return ChatRoomResponse.of(chatRoom, chatRoomName, thumbnail, participants);
    }

    private String resolveChatRoomName(ChatRoom chatRoom, Long userId) {
        if (chatRoom.getChatType() == ChatType.GROUP && chatRoom.getGroup() != null) {
            return chatRoom.getGroup().getTitle();
        }

        // DM인 경우 상대방 이름 반환
        return chatParticipantRepository.findActiveParticipants(chatRoom.getId())
                .stream()
                .filter(p -> !p.getUser().getId().equals(userId))
                .findFirst()
                .map(p -> p.getUser().getNickName())
                .orElse("알 수 없음");
    }

    private String resolveThumbnail(ChatRoom chatRoom, Long userId) {
        if (chatRoom.getChatType() == ChatType.GROUP && chatRoom.getGroup() != null) {
            // 그룹 채팅: 그룹의 첫 번째 이미지의 THUMBNAIL_100_100 variant
            List<GroupImageV2> images = groupImageV2Repository
                    .findAllByGroupIdWithVariants(chatRoom.getGroup().getId());

            if (images.isEmpty()) {
                return null;
            }

            return images.get(0).getVariants().stream()
                    .filter(v -> v.getType() == GroupImageV2VariantType.THUMBNAIL_100_100)
                    .findFirst()
                    .map(v -> v.getImageUrl())
                    .orElse(null);
        }

        // DM인 경우 상대방 프로필 이미지 반환
        return chatParticipantRepository.findActiveParticipants(chatRoom.getId())
                .stream()
                .filter(p -> !p.getUser().getId().equals(userId))
                .findFirst()
                .map(p -> p.getUser().getProfileImage())
                .orElse(null);
    }

    private boolean isOwner(ChatParticipant participant, Long hostId) {
        if (hostId == null) {
            return false;
        }
        return participant.getUser().getId().equals(hostId);
    }

    private int calculateUnreadCount(Long roomId, Long userId) {
        return chatParticipantRepository.findByChatRoomIdAndUserId(roomId, userId)
                .filter(ChatParticipant::isActive)
                .map(participant -> {
                    Long lastReadId = participant.getLastReadMessageId();
                    if (lastReadId == null) {
                        lastReadId = 0L;
                    }
                    return chatMessageRepository.countUnreadMessages(roomId, lastReadId);
                })
                .orElse(0);
    }

    private ChatRoomResponse createDmRoom(User user, User targetUser) {
        ChatRoom chatRoom = ChatRoom.createDmChat();
        chatRoomRepository.save(chatRoom);

        ChatParticipant participant1 = ChatParticipant.create(user, JoinType.MANUAL);
        ChatParticipant participant2 = ChatParticipant.create(targetUser, JoinType.MANUAL);

        chatRoom.addParticipant(participant1);
        chatRoom.addParticipant(participant2);

        log.info("DM 채팅방 생성 - chatRoomId: {}, users: [{}, {}]",
                chatRoom.getId(), user.getId(), targetUser.getId());

        return buildChatRoomResponse(chatRoom, user.getId());
    }
}
