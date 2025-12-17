package team.wego.wegobackend.group.v2.domain.repository;

import java.util.List;
import java.util.Map;
import team.wego.wegobackend.group.v2.infrastructure.querydsl.projection.GroupListRow;

public interface GroupV2QueryRepository {

    List<GroupListRow> fetchGroupRows(String keyword, Long cursor, int limit);

    Map<Long, List<String>> fetchTagNamesByGroupIds(List<Long> groupIds);

    Map<Long, List<String>> fetchMainImageUrlsByGroupIds(List<Long> groupIds, int perGroupLimit);
}

