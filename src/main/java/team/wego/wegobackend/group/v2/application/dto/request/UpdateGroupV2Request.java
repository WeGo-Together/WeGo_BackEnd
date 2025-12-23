package team.wego.wegobackend.group.v2.application.dto.request;

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
        @Size(max = 50)
        String title,
        @Size(max = 300)
        String description,

        String location,
        String locationDetail,

        LocalDateTime startTime,
        LocalDateTime endTime,

        Integer maxParticipants,

        GroupV2Status status,

        @Size(max = 10)
        List<String> tags,

        @Size(max = 3)
        List<String> images
) {

}
