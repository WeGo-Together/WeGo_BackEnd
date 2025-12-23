package team.wego.wegobackend.group.v2.application.dto.response;


import java.time.LocalDateTime;
import java.util.List;
import team.wego.wegobackend.group.v2.application.dto.common.AttendanceTargetItem;

public record GetKickTargetsResponse(
        Long groupId,
        List<AttendanceTargetItem> targets,
        LocalDateTime serverTime
) {

    public static GetKickTargetsResponse of(Long groupId, List<AttendanceTargetItem> targets) {
        return new GetKickTargetsResponse(groupId, targets, LocalDateTime.now());
    }
}
