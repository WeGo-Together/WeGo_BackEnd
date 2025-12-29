package team.wego.wegobackend.group.v2.application.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;
import team.wego.wegobackend.group.v2.domain.entity.GroupV2Status;

/**
 *
 * images는 “최종 순서 리스트”로 받자! (0번이 대표) 기존 imageKey + 새 preUploaded imageKey를 섞어서 보내도 OK! 생략(null)이면
 * “이미지 변경 없음” 으로 가자 빈 리스트([])면 “이미지 전체 삭제”(정책 허용 시) 하자
 */
public record UpdateGroupV2Request(
        @Size(max = 50, message = "모임: 모임 제목은 50자 이하 입니다.")
        String title,
        @Size(max = 300, message = "모임: 모임 설명은 300자 이하 입니다.")
        String description,

        String location,
        String locationDetail,

        @FutureOrPresent(message = "모임: 시작 시간은 현재 이후여야 합니다.")
        LocalDateTime startTime,

        @Future(message = "모임: 종료 시간은 현재 이후여야 합니다.")
        LocalDateTime endTime,

        @Min(value = 2, message = "모임: 최대 인원은 최소 2명 이상이어야 합니다.")
        @Max(value = 12, message = "모임: 최대 인원은 최대 12명 이하이어야 합니다.")
        Integer maxParticipants,

        GroupV2Status status,

        @Size(max = 10)
        List<String> tags,

        @Valid
        List<CreateGroupImageV2Request> images
) {

}
