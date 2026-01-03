package team.wego.wegobackend.chat.application.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import team.wego.wegobackend.chat.domain.entity.ChatRoom;
import team.wego.wegobackend.chat.domain.entity.ChatType;

public record ChatRoomResponse(
        Long chatRoomId,
        ChatType chatType,
        String chatRoomName,
        String thumbnail,
        Long groupId,
        int participantCount,
        List<ParticipantResponse> participants,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ChatRoomResponse of(
            ChatRoom chatRoom,
            String chatRoomName,
            String thumbnail,
            List<ParticipantResponse> participants
    ) {
        return new ChatRoomResponse(
                chatRoom.getId(),
                chatRoom.getChatType(),
                chatRoomName,
                thumbnail,
                chatRoom.getGroup() != null ? chatRoom.getGroup().getId() : null,
                participants.size(),
                participants,
                chatRoom.getCreatedAt(),
                chatRoom.getUpdatedAt()
        );
    }
}
