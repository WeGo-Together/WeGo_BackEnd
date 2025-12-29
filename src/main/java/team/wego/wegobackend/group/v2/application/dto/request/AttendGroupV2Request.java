package team.wego.wegobackend.group.v2.application.dto.request;

import jakarta.validation.constraints.Size;

public record AttendGroupV2Request(
        @Size(max = 300, message = "모임: 모임 메시지는 최대 300자 이하입니다.")
        String message
) {

}

