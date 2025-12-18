package team.wego.wegobackend.group.v2.domain.repository;

import java.util.List;
import java.util.Map;
import team.wego.wegobackend.group.v2.domain.entity.GroupV2Status;
import team.wego.wegobackend.group.v2.infrastructure.querydsl.projection.GroupListRow;

public interface GroupV2QueryRepository {

    List<GroupListRow> fetchGroupRows(
            String keyword,
            Long cursor,
            int limit,
            List<GroupV2Status> includeStatuses,
            List<GroupV2Status> excludeStatuses
    );

    Map<Long, List<String>> fetchTagNamesByGroupIds(List<Long> groupIds);

    Map<Long, List<String>> fetchMainImageUrlsByGroupIds(List<Long> groupIds, int perGroupLimit);
}

