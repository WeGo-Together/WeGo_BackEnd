package team.wego.wegobackend.group.v2.domain.repository;

import java.util.List;
import team.wego.wegobackend.group.v2.domain.entity.GroupUserV2Status;
import team.wego.wegobackend.group.v2.infrastructure.querydsl.projection.AttendanceTargetRow;
import team.wego.wegobackend.group.v2.infrastructure.querydsl.projection.JoinRequestRow;

public interface GroupUserV2QueryRepository {

    List<AttendanceTargetRow> fetchAttendMembersExceptHost(Long groupId);

    List<AttendanceTargetRow> fetchBannedMembersExceptHost(Long groupId);

    List<JoinRequestRow> fetchJoinRequests(Long groupId, GroupUserV2Status status);
}

