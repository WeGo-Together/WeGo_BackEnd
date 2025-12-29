package team.wego.wegobackend.chat.presentation;

import java.security.Principal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;
import team.wego.wegobackend.chat.application.dto.request.SendMessageRequest;
import team.wego.wegobackend.chat.application.dto.response.ChatMessagePayload;
import team.wego.wegobackend.chat.application.service.ChatMessageService;
import team.wego.wegobackend.chat.config.StompChannelInterceptor.StompPrincipal;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatMessageController {

    private final SimpMessageSendingOperations messagingTemplate;
    private final ChatMessageService chatMessageService;

    /**
     * 메시지 전송 처리
     * 클라이언트에서 /pub/chat/message 로 전송
     * 구독자들은 /sub/chat/room/{roomId} 로 수신
     */
    @MessageMapping("/chat/message")
    public void sendMessage(@Payload SendMessageRequest request, Principal principal) {
        if (principal == null) {
            log.warn("인증되지 않은 사용자의 메시지 전송 시도");
            return;
        }

        StompPrincipal stompPrincipal = (StompPrincipal) principal;
        Long userId = stompPrincipal.userId();

        log.debug("메시지 수신 - roomId: {}, userId: {}, content: {}",
                request.chatRoomId(), userId, request.content());

        ChatMessagePayload payload = chatMessageService.sendMessage(
                userId, request.chatRoomId(), request.content()
        );

        // 채팅방 구독자들에게 메시지 브로드캐스트
        messagingTemplate.convertAndSend(
                "/sub/chat/room/" + request.chatRoomId(),
                payload
        );
    }

    /**
     * 입장 메시지 전송 (시스템 메시지)
     */
    public void sendEnterMessage(Long chatRoomId, String userName) {
        ChatMessagePayload payload = ChatMessagePayload.systemMessage(
                chatRoomId,
                userName + "님이 입장했습니다"
        );

        messagingTemplate.convertAndSend(
                "/sub/chat/room/" + chatRoomId,
                payload
        );
    }

    /**
     * 퇴장 메시지 전송 (시스템 메시지)
     */
    public void sendLeaveMessage(Long chatRoomId, String userName) {
        ChatMessagePayload payload = ChatMessagePayload.systemMessage(
                chatRoomId,
                userName + "님이 퇴장했습니다"
        );

        messagingTemplate.convertAndSend(
                "/sub/chat/room/" + chatRoomId,
                payload
        );
    }

    /**
     * 특정 사용자에게 개인 알림 전송
     */
    public void sendToUser(Long userId, Object payload) {
        messagingTemplate.convertAndSend(
                "/sub/user/" + userId,
                payload
        );
    }
}
