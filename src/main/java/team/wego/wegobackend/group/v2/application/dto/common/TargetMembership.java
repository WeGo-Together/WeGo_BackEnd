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
    public static TargetMembership of(Long userId, GroupUserV2 groupUserV2) {
        return new TargetMembership(
                userId,
                groupUserV2.getId(),
                groupUserV2.getStatus(),
                groupUserV2.getJoinedAt(),
                groupUserV2.getLeftAt()
        );
    }
}