package team.wego.wegobackend.group.v2.application.dto.common;

import java.time.LocalDateTime;
import team.wego.wegobackend.group.v2.domain.entity.GroupUserV2;
import team.wego.wegobackend.group.v2.domain.entity.GroupUserV2Status;

public record TargetMembership(
        Long userId,
        Long groupUserId,
        GroupUserV2Status status,
        LocalDateTime joinedAt,
        LocalDateTime leftAt
) {
    public static TargetMembership from(GroupUserV2 gu) {
        return new TargetMembership(
                gu.getUser().getId(),
                gu.getId(),
                gu.getStatus(),
                gu.getJoinedAt(),
                gu.getLeftAt()
        );
    }
}