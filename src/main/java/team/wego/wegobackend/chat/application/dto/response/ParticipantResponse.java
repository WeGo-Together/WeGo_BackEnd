package team.wego.wegobackend.chat.application.dto.response;

import java.time.LocalDateTime;
import team.wego.wegobackend.chat.domain.entity.ChatParticipant;
import team.wego.wegobackend.chat.domain.entity.ParticipantStatus;

public record ParticipantResponse(
        Long participantId,
        Long userId,
        String nickName,
        String profileImage,
        ParticipantStatus status,
        LocalDateTime joinedAt
) {
    public static ParticipantResponse from(ChatParticipant participant) {
        return new ParticipantResponse(
                participant.getId(),
                participant.getUser().getId(),
                participant.getUser().getNickName(),
                participant.getUser().getProfileImage(),
                participant.getStatus(),
                participant.getJoinedAt()
        );
    }
}
