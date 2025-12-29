package team.wego.wegobackend.chat.application.dto.response;

public record ReadStatusResponse(
        Long chatRoomId,
        Long lastReadMessageId,
        int unreadCount
) {
    public static ReadStatusResponse of(Long chatRoomId, Long lastReadMessageId, int unreadCount) {
        return new ReadStatusResponse(chatRoomId, lastReadMessageId, unreadCount);
    }
}
