package team.wego.wegobackend.group.v2.domain.repository;

import java.util.List;
import team.wego.wegobackend.group.v2.infrastructure.querydsl.projection.AttendanceTargetRow;

public interface GroupUserV2QueryRepository {

    List<AttendanceTargetRow> fetchAttendMembersExceptHost(Long groupId);

    List<AttendanceTargetRow> fetchBannedMembersExceptHost(Long groupId);
}

