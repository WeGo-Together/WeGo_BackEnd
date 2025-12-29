package team.wego.wegobackend.chat.application.dto.response;

import java.util.List;

public record ChatRoomListResponse(
        List<ChatRoomItemResponse> chatRooms
) {
    public static ChatRoomListResponse from(List<ChatRoomItemResponse> chatRooms) {
        return new ChatRoomListResponse(chatRooms);
    }
}
