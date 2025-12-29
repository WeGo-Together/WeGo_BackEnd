package team.wego.wegobackend.chat.application.dto.response;

import java.time.LocalDateTime;
import team.wego.wegobackend.chat.domain.entity.ChatMessage;
import team.wego.wegobackend.chat.domain.entity.MessageType;

/**
 * WebSocket을 통해 클라이언트로 전송되는 메시지 페이로드
 */
public record ChatMessagePayload(
        Long messageId,
        Long chatRoomId,
        Long senderId,
        String senderName,
        String senderProfileImage,
        String content,
        MessageType messageType,
        LocalDateTime timestamp
) {
    public static ChatMessagePayload from(ChatMessage message) {
        return new ChatMessagePayload(
                message.getId(),
                message.getChatRoom().getId(),
                message.getSender() != null ? message.getSender().getId() : null,
                message.getSender() != null ? message.getSender().getNickName() : null,
                message.getSender() != null ? message.getSender().getProfileImage() : null,
                message.getContent(),
                message.getMessageType(),
                message.getCreatedAt()
        );
    }

    public static ChatMessagePayload systemMessage(Long chatRoomId, String content) {
        return new ChatMessagePayload(
                null,
                chatRoomId,
                null,
                null,
                null,
                content,
                MessageType.SYSTEM,
                LocalDateTime.now()
        );
    }
}
