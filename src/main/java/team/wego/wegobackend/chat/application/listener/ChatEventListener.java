package team.wego.wegobackend.chat.application.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import team.wego.wegobackend.chat.application.service.ChatRoomService;
import team.wego.wegobackend.chat.config.ChatProperties;
import team.wego.wegobackend.chat.domain.entity.JoinType;
import team.wego.wegobackend.group.v2.application.event.GroupCreatedEvent;
import team.wego.wegobackend.group.v2.application.event.GroupJoinedEvent;
import team.wego.wegobackend.group.v2.application.event.GroupLeftEvent;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChatEventListener {

    private final ChatRoomService chatRoomService;
    private final ChatProperties chatProperties;

    /**
     * 모임 생성 시 그룹 채팅방 자동 생성
     */
    @EventListener
    @Async
    public void handleGroupCreated(GroupCreatedEvent event) {
        log.info("모임 생성 이벤트 수신 - groupId: {}, hostUserId: {}",
                event.groupId(), event.hostUserId());

        try {
            chatRoomService.createGroupChatRoomForMeeting(
                    event.groupId(),
                    event.hostUserId()
            );
            log.info("그룹 채팅방 생성 완료 - groupId: {}", event.groupId());
        } catch (Exception e) {
            log.error("그룹 채팅방 생성 실패 - groupId: {}", event.groupId(), e);
        }
    }

    /**
     * 모임 참여 시 채팅방 자동 참여
     */
    @EventListener
    @Async
    public void handleGroupJoined(GroupJoinedEvent event) {
        log.info("모임 참여 이벤트 수신 - groupId: {}, joinerUserId: {}",
                event.groupId(), event.joinerUserId());

        if (!chatProperties.getAutoJoin().isEnabled()) {
            log.debug("자동 참여 비활성화 - groupId: {}", event.groupId());
            return;
        }

        try {
            chatRoomService.joinChatRoomByGroup(
                    event.groupId(),
                    event.joinerUserId(),
                    JoinType.AUTO
            );
            log.info("채팅방 자동 참여 완료 - groupId: {}, userId: {}",
                    event.groupId(), event.joinerUserId());
        } catch (Exception e) {
            log.error("채팅방 자동 참여 실패 - groupId: {}, userId: {}",
                    event.groupId(), event.joinerUserId(), e);
        }
    }

    /**
     * 모임 퇴장 시 채팅방 퇴장 처리 (선택 사항)
     */
    @EventListener
    @Async
    public void handleGroupLeft(GroupLeftEvent event) {
        log.info("모임 퇴장 이벤트 수신 - groupId: {}, leaverUserId: {}",
                event.groupId(), event.leaverUserId());

        try {
            chatRoomService.leaveChatRoomByGroup(
                    event.groupId(),
                    event.leaverUserId()
            );
            log.info("채팅방 퇴장 처리 완료 - groupId: {}, userId: {}",
                    event.groupId(), event.leaverUserId());
        } catch (Exception e) {
            log.error("채팅방 퇴장 처리 실패 - groupId: {}, userId: {}",
                    event.groupId(), event.leaverUserId(), e);
        }
    }
}
