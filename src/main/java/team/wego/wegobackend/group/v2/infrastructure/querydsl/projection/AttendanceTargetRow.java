package team.wego.wegobackend.group.v2.infrastructure.querydsl.projection;

import java.time.LocalDateTime;
import team.wego.wegobackend.group.v2.domain.entity.GroupUserV2Status;

public record AttendanceTargetRow(
        Long userId,
        String nickName,
        String profileImage,
        Long groupUserId,
        GroupUserV2Status status,
        LocalDateTime joinedAt,
        LocalDateTime leftAt
) {

}

