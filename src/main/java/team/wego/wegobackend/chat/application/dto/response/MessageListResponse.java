package team.wego.wegobackend.chat.application.dto.response;

import java.util.List;

public record MessageListResponse(
        List<MessageResponse> messages,
        boolean hasNext,
        Long nextCursor
) {
    public static MessageListResponse of(
            List<MessageResponse> messages,
            boolean hasNext,
            Long nextCursor
    ) {
        return new MessageListResponse(messages, hasNext, nextCursor);
    }
}
