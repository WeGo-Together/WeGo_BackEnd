package team.wego.wegobackend.group.v2.application.dto.common;

import java.time.LocalDateTime;
import java.util.List;
import team.wego.wegobackend.group.v2.domain.entity.GroupUserV2;
import team.wego.wegobackend.group.v2.domain.entity.GroupUserV2Role;
import team.wego.wegobackend.group.v2.domain.entity.GroupUserV2Status;

public record MyMembership(
        Long groupUserId,
        GroupUserV2Role role,
        GroupUserV2Status status,
        LocalDateTime joinedAt,
        LocalDateTime leftAt
) {

    public static MyMembership from(List<GroupUserV2> users, Long userId) {
        return users.stream()
                .filter(groupUserV2 -> groupUserV2.getUser().getId().equals(userId))
                .findFirst()
                .map(groupUserV2 -> new MyMembership(
                        groupUserV2.getId(),
                        groupUserV2.getGroupRole(),
                        groupUserV2.getStatus(),
                        groupUserV2.getJoinedAt(),
                        groupUserV2.getLeftAt()
                ))
                .orElse(new MyMembership(null, null, null, null, null));
    }
}