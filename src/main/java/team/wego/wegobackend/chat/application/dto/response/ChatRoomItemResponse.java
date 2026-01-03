package team.wego.wegobackend.chat.application.dto.response;

import java.time.LocalDateTime;
import team.wego.wegobackend.chat.domain.entity.ChatRoom;
import team.wego.wegobackend.chat.domain.entity.ChatType;

public record ChatRoomItemResponse(
        Long chatRoomId,
        ChatType chatType,
        String chatRoomName,
        String thumbnail,
        Long groupId,
        int participantCount,
        LastMessageResponse lastMessage,
        int unreadCount,
        LocalDateTime updatedAt
) {
    public static ChatRoomItemResponse of(
            ChatRoom chatRoom,
            String chatRoomName,
            String thumbnail,
            int participantCount,
            LastMessageResponse lastMessage,
            int unreadCount
    ) {
        return new ChatRoomItemResponse(
                chatRoom.getId(),
                chatRoom.getChatType(),
                chatRoomName,
                thumbnail,
                chatRoom.getGroup() != null ? chatRoom.getGroup().getId() : null,
                participantCount,
                lastMessage,
                unreadCount,
                chatRoom.getUpdatedAt()
        );
    }
}
