package team.wego.wegobackend.chat.application.dto.request;

import jakarta.validation.constraints.NotNull;

public record CreateDmRequest(
        @NotNull(message = "대상 사용자 ID는 필수입니다")
        Long targetUserId
) {
}
