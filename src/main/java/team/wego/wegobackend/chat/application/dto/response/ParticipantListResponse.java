package team.wego.wegobackend.chat.application.dto.response;

import java.util.List;

public record ParticipantListResponse(
        Long chatRoomId,
        int totalCount,
        List<ParticipantResponse> participants
) {
    public static ParticipantListResponse of(
            Long chatRoomId,
            List<ParticipantResponse> participants
    ) {
        return new ParticipantListResponse(chatRoomId, participants.size(), participants);
    }
}
