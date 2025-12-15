package team.wego.wegobackend.group.application.dto.v1.request;


import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

public record UpdateGroupRequest(
        @NotBlank(message = "모임: 제목은 필수 입니다.")
        String title,

        @NotBlank(message = "모임: 모임 위치는 필수 입니다.")
        String location,

        String locationDetail,

        @NotNull(message = "모임: 시작 시간은 필수 입니다.")
        @FutureOrPresent(message = "모임: 시작 시간은 현재 이후여야 합니다.")
        LocalDateTime startTime,

        @Future(message = "모임: 종료 시간은 현재 이후여야 합니다.")
        LocalDateTime endTime,

        List<String> tags,

        @NotBlank(message = "모임: 설명은 필수 입니다.")
        String description,

        @NotNull(message = "모임: 최대 인원은 필수 입니다.")
        @Min(value = 2, message = "모임: 최대 인원은 최소 2명 이상이어야 합니다.")
        @Max(value = 12, message = "모임: 최대 인원은 최대 12명 이하이어야 합니다.")
        Integer maxParticipants
) {}