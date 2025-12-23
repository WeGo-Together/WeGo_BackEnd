package team.wego.wegobackend.group.v2.application.dto.response;

import java.time.LocalDateTime;
import team.wego.wegobackend.group.v2.application.dto.common.TargetMembership;
import team.wego.wegobackend.group.v2.domain.entity.GroupUserV2;
import team.wego.wegobackend.group.v2.domain.entity.GroupV2;
import team.wego.wegobackend.group.v2.domain.entity.GroupV2JoinPolicy;
import team.wego.wegobackend.group.v2.domain.entity.GroupV2Status;

public record GroupUserV2StatusResponse(
        Long groupId,
        GroupV2Status groupStatus,
        GroupV2JoinPolicy joinPolicy,
        long participantCount,
        int maxParticipants,
        TargetMembership targetMembership,
        LocalDateTime serverTime
) {

    public static GroupUserV2StatusResponse of(
            GroupV2 group,
            long participantCount,
            Long targetUserId,
            GroupUserV2 target
    ) {
        return new GroupUserV2StatusResponse(
                group.getId(),
                group.getStatus(),
                group.getJoinPolicy(),
                participantCount,
                group.getMaxParticipants(),
                TargetMembership.from(targetUserId, target),
                LocalDateTime.now()
        );
    }
}

