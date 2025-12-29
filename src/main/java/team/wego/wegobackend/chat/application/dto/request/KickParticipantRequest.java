package team.wego.wegobackend.chat.application.dto.request;

import jakarta.validation.constraints.NotNull;

public record KickParticipantRequest(
        @NotNull(message = "추방 대상 사용자 ID는 필수입니다")
        Long targetUserId
) {
}
