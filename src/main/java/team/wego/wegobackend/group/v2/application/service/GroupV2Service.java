package team.wego.wegobackend.group.v2.application.service;

import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.wego.wegobackend.group.v2.application.dto.response.GetGroupListV2Response;
import team.wego.wegobackend.group.v2.application.dto.response.GetGroupListV2Response.GroupListItemV2Response;
import team.wego.wegobackend.group.v2.application.dto.response.GetGroupListV2Response.GroupListItemV2Response.CreatedByV2Response;
import team.wego.wegobackend.group.v2.domain.repository.GroupV2QueryRepository;
import team.wego.wegobackend.group.v2.infrastructure.querydsl.projection.GroupListRow;

@RequiredArgsConstructor
@Service
public class GroupV2Service {

    private final GroupV2QueryRepository groupV2QueryRepository;

    private static final int MAX_PAGE_SIZE = 50;
    private static final int GROUP_LIST_IMAGE_LIMIT = 3;

    @Transactional(readOnly = true)
    public GetGroupListV2Response getGroupListV2(String keyword, Long cursor, int size) {

        int pageSize = Math.max(1, Math.min(size, MAX_PAGE_SIZE));

        List<GroupListRow> rows = groupV2QueryRepository.fetchGroupRows(
                keyword,
                cursor,
                pageSize + 1
        );

        boolean hasNext = rows.size() > pageSize;
        List<GroupListRow> content = hasNext ? rows.subList(0, pageSize) : rows;
        Long nextCursor = hasNext ? content.getLast().groupId() : null;

        if (content.isEmpty()) {
            return GetGroupListV2Response.of(List.of(), null);
        }

        List<Long> groupIds = content.stream().map(GroupListRow::groupId).toList();

        Map<Long, List<String>> imageMap =
                groupV2QueryRepository.fetchMainImageUrlsByGroupIds(groupIds,
                        GROUP_LIST_IMAGE_LIMIT);

        Map<Long, List<String>> tagMap =
                groupV2QueryRepository.fetchTagNamesByGroupIds(groupIds);

        List<GroupListItemV2Response> items = content.stream()
                .map(r -> {
                    int participantCount =
                            (r.participantCount() == null) ? 0 : r.participantCount().intValue();

                    return GroupListItemV2Response.of(
                            r.groupId(),
                            r.title(),
                            r.location(),
                            r.locationDetail(),
                            r.startTime(),
                            r.endTime(),
                            imageMap.getOrDefault(r.groupId(), List.of()),
                            tagMap.getOrDefault(r.groupId(), List.of()),
                            r.description(),
                            participantCount,
                            r.maxParticipants(),
                            CreatedByV2Response.of(r.hostId(), r.hostNickName(),
                                    r.hostProfileImage()),
                            r.createdAt(),
                            r.updatedAt()
                    );
                })
                .toList();

        return GetGroupListV2Response.of(items, nextCursor);
    }
}
