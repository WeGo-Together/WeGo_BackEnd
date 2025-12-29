package team.wego.wegobackend.chat.application.dto.response;

import java.time.LocalDateTime;
import team.wego.wegobackend.chat.domain.entity.ChatMessage;

public record LastMessageResponse(
        String content,
        String senderName,
        LocalDateTime timestamp
) {
    public static LastMessageResponse from(ChatMessage message, String senderName) {
        return new LastMessageResponse(
                message.getContent(),
                senderName,
                message.getCreatedAt()
        );
    }
}
