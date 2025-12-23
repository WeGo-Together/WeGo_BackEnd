package team.wego.wegobackend.group.v2.application.dto.common;

import team.wego.wegobackend.group.v2.domain.entity.GroupUserV2;
import team.wego.wegobackend.group.v2.domain.entity.GroupUserV2Status;

public record TargetMembership(
        Long userId,
        Long groupUserId,
        GroupUserV2Status status
) {

    public static TargetMembership of(Long userId, Long groupUserId, GroupUserV2Status status) {
        return new TargetMembership(userId, groupUserId, status);
    }

    public static TargetMembership from(Long userId, GroupUserV2 target) {
        return new TargetMembership(
                userId,
                target.getId(),
                target.getStatus()
        );
    }
}

