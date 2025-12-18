package team.wego.wegobackend.group.v2.application.dto.request;

import jakarta.validation.constraints.Min;

public record CreateGroupImageV2Request(
        String imageKey,

        @Min(value = 0, message = "모임 이미지: 모임 이미지 순서는 0 이상 숫자만 가능합니다.")
        Integer sortOrder
) {}
