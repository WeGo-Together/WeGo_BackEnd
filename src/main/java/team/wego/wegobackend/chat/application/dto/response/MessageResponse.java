package team.wego.wegobackend.chat.application.dto.response;

import java.time.LocalDateTime;
import team.wego.wegobackend.chat.domain.entity.ChatMessage;
import team.wego.wegobackend.chat.domain.entity.MessageType;

public record MessageResponse(
        Long messageId,
        Long senderId,
        String senderName,
        String senderProfileImage,
        String content,
        MessageType messageType,
        LocalDateTime createdAt
) {
    public static MessageResponse from(ChatMessage message) {
        return new MessageResponse(
                message.getId(),
                message.getSender() != null ? message.getSender().getId() : null,
                message.getSender() != null ? message.getSender().getNickName() : null,
                message.getSender() != null ? message.getSender().getProfileImage() : null,
                message.getContent(),
                message.getMessageType(),
                message.getCreatedAt()
        );
    }
}
