package team.wego.wegobackend.group.v2.application.dto.response;

import java.time.LocalDateTime;
import team.wego.wegobackend.group.v2.application.dto.common.TargetMembership;
import team.wego.wegobackend.group.v2.domain.entity.GroupUserV2;
import team.wego.wegobackend.group.v2.domain.entity.GroupV2;
import team.wego.wegobackend.group.v2.domain.entity.GroupV2JoinPolicy;
import team.wego.wegobackend.group.v2.domain.entity.GroupV2Status;

public record ApproveRejectGroupV2Response(
        Long groupId,
        GroupV2Status groupStatus,
        GroupV2JoinPolicy joinPolicy,
        long participantCount,
        int maxParticipants,

        TargetMembership targetMembership,

        LocalDateTime serverTime
) {

    public static ApproveRejectGroupV2Response of(
            GroupV2 group,
            long participantCount,
            GroupUserV2 target
    ) {
        return new ApproveRejectGroupV2Response(
                group.getId(),
                group.getStatus(),
                group.getJoinPolicy(),
                participantCount,
                group.getMaxParticipants(),
                TargetMembership.from(target),
                LocalDateTime.now()
        );
    }
}