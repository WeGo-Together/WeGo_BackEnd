package team.wego.wegobackend.group.v2.application.dto.response;

import java.time.LocalDateTime;
import team.wego.wegobackend.group.v2.application.dto.common.MyMembership;
import team.wego.wegobackend.group.v2.domain.entity.GroupV2;
import team.wego.wegobackend.group.v2.domain.entity.GroupV2Status;

public record AttendGroupV2Response(
        Long groupId,
        GroupV2Status groupStatus,
        long participantCount,
        int maxParticipants,
        MyMembership myMembership,
        LocalDateTime serverTime
) {

    public static AttendGroupV2Response of(
            GroupV2 group,
            long participantCount,
            MyMembership myMembership
    ) {
        return new AttendGroupV2Response(
                group.getId(),
                group.getStatus(),
                participantCount,
                group.getMaxParticipants(),
                myMembership,
                LocalDateTime.now()
        );
    }
}