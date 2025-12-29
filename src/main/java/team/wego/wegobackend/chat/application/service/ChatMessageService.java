package team.wego.wegobackend.chat.application.service;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.wego.wegobackend.chat.application.dto.response.ChatMessagePayload;
import team.wego.wegobackend.chat.application.dto.response.MessageListResponse;
import team.wego.wegobackend.chat.application.dto.response.MessageResponse;
import team.wego.wegobackend.chat.config.ChatProperties;
import team.wego.wegobackend.chat.domain.entity.ChatMessage;
import team.wego.wegobackend.chat.domain.entity.ChatRoom;
import team.wego.wegobackend.chat.domain.entity.ParticipantStatus;
import team.wego.wegobackend.chat.domain.exception.ChatErrorCode;
import team.wego.wegobackend.chat.domain.exception.ChatException;
import team.wego.wegobackend.chat.domain.repository.ChatMessageRepository;
import team.wego.wegobackend.chat.domain.repository.ChatParticipantRepository;
import team.wego.wegobackend.chat.domain.repository.ChatRoomRepository;
import team.wego.wegobackend.user.domain.User;
import team.wego.wegobackend.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ChatMessageService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final ChatProperties chatProperties;

    /**
     * 메시지 전송
     */
    @Transactional
    public ChatMessagePayload sendMessage(Long userId, Long roomId, String content) {
        // 메시지 내용 검증
        validateMessageContent(content);

        // 채팅방 및 참여자 검증
        ChatRoom chatRoom = findChatRoomById(roomId);
        validateParticipant(roomId, userId);

        User sender = findUserById(userId);

        // 메시지 생성 및 저장
        ChatMessage message = ChatMessage.createTextMessage(sender, content);
        chatRoom.addMessage(message);
        chatMessageRepository.save(message);

        log.debug("메시지 저장 - roomId: {}, messageId: {}, senderId: {}",
                roomId, message.getId(), userId);

        return ChatMessagePayload.from(message);
    }

    /**
     * 시스템 메시지 전송 (입장/퇴장 등)
     */
    @Transactional
    public ChatMessagePayload sendSystemMessage(Long roomId, String content) {
        ChatRoom chatRoom = findChatRoomById(roomId);

        ChatMessage message = ChatMessage.createSystemMessage(content);
        chatRoom.addMessage(message);
        chatMessageRepository.save(message);

        log.debug("시스템 메시지 저장 - roomId: {}, content: {}", roomId, content);

        return ChatMessagePayload.from(message);
    }

    /**
     * 메시지 이력 조회 (커서 기반 페이징)
     */
    public MessageListResponse getMessages(Long userId, Long roomId, Long cursor, int size) {
        // 참여자 검증
        validateParticipant(roomId, userId);

        PageRequest pageRequest = PageRequest.of(0, size);
        Slice<ChatMessage> messageSlice;

        if (cursor == null) {
            // 첫 페이지: 최신 메시지부터
            messageSlice = chatMessageRepository.findByChatRoomIdOrderByCreatedAtDesc(roomId, pageRequest);
        } else {
            // 이후 페이지: 커서 이전 메시지
            messageSlice = chatMessageRepository.findByChatRoomIdAndIdLessThan(roomId, cursor, pageRequest);
        }

        List<MessageResponse> messages = messageSlice.getContent()
                .stream()
                .map(MessageResponse::from)
                .collect(Collectors.toList());

        Long nextCursor = messageSlice.hasNext() && !messages.isEmpty()
                ? messages.get(messages.size() - 1).messageId()
                : null;

        return MessageListResponse.of(messages, messageSlice.hasNext(), nextCursor);
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

    private void validateParticipant(Long roomId, Long userId) {
        boolean isParticipant = chatParticipantRepository
                .existsByChatRoomIdAndUserIdAndStatus(roomId, userId, ParticipantStatus.ACTIVE);

        if (!isParticipant) {
            throw new ChatException(ChatErrorCode.NOT_CHAT_PARTICIPANT);
        }
    }

    private void validateMessageContent(String content) {
        if (content == null || content.isBlank()) {
            throw new ChatException(ChatErrorCode.MESSAGE_EMPTY);
        }

        int maxLength = chatProperties.getMessage().getMaxLength();
        if (content.length() > maxLength) {
            throw new ChatException(ChatErrorCode.MESSAGE_TOO_LONG, maxLength);
        }
    }
}
