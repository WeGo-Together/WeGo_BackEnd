package team.wego.wegobackend.group.v2.application.dto.request;

import jakarta.validation.constraints.Max;

public record CreateGroupImageV2Request(
        String imageKey,

        @Max(value = 3, message = "모임 이미지: 모임 이미지 순서는 2 이하만 가능합니다.")
        Integer sortOrder
) {

}
