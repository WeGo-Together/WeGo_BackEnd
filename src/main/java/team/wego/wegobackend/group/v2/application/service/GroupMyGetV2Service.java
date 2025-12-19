package team.wego.wegobackend.group.v2.application.service;

import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.wego.wegobackend.group.domain.exception.GroupErrorCode;
import team.wego.wegobackend.group.domain.exception.GroupException;
import team.wego.wegobackend.group.v2.application.dto.common.CreatedBy;
import team.wego.wegobackend.group.v2.application.dto.common.MyMembership;
import team.wego.wegobackend.group.v2.application.dto.request.GroupListFilter;
import team.wego.wegobackend.group.v2.application.dto.request.MyGroupTypeV2;
import team.wego.wegobackend.group.v2.application.dto.response.GetMyGroupListV2Response;
import team.wego.wegobackend.group.v2.domain.entity.GroupUserV2Status;
import team.wego.wegobackend.group.v2.domain.entity.GroupV2Status;
import team.wego.wegobackend.group.v2.domain.repository.GroupV2QueryRepository;
import team.wego.wegobackend.group.v2.infrastructure.querydsl.projection.MyGroupListRow;

@RequiredArgsConstructor
@Service
public class GroupMyGetV2Service {

    private final GroupV2QueryRepository groupV2QueryRepository;

    private static final int MAX_PAGE_SIZE = 50;
    private static final int GROUP_LIST_IMAGE_LIMIT = 3;

    @Transactional(readOnly = true)
    public GetMyGroupListV2Response getMyGroups(
            Long userId,
            Long cursor,
            int size,
            MyGroupTypeV2 type,
            GroupListFilter filter,
            List<GroupV2Status> includeStatuses,
            List<GroupV2Status> excludeStatuses,
            List<GroupUserV2Status> myStatuses
    ) {
        if (userId == null) {
            throw new GroupException(GroupErrorCode.USER_ID_NULL);
        }

        type = (type == null) ? MyGroupTypeV2.CURRENT : type;

        // type에 맞는 기본 filter (프론트 탭 의미 보장)
        if (filter == null) {
            filter = switch (type) {
                case CURRENT, MY_POST -> GroupListFilter.ACTIVE;
                case PAST -> GroupListFilter.ARCHIVED;
            };
        }

        int pageSize = Math.max(1, Math.min(size, MAX_PAGE_SIZE));
        ResolvedStatuses resolved = resolveStatuses(filter, includeStatuses, excludeStatuses);

        List<MyGroupListRow> rows;
        if (type == MyGroupTypeV2.MY_POST) {
            rows = groupV2QueryRepository.fetchMyPostGroupRows(
                    userId,
                    cursor,
                    pageSize + 1,
                    resolved.includes(),
                    resolved.excludes()
            );
        } else {
            // 내 상태 기본: ATTEND (요청 없으면)
            if (myStatuses == null || myStatuses.isEmpty()) {
                myStatuses = List.of(GroupUserV2Status.ATTEND);
            }

            rows = groupV2QueryRepository.fetchMyGroupRows(
                    userId,
                    cursor,
                    pageSize + 1,
                    resolved.includes(),
                    resolved.excludes(),
                    myStatuses
            );
        }

        boolean hasNext = rows.size() > pageSize;
        List<MyGroupListRow> content = hasNext ? rows.subList(0, pageSize) : rows;
        Long nextCursor = hasNext ? content.getLast().groupId() : null;

        if (content.isEmpty()) {
            return GetMyGroupListV2Response.of(List.of(), null);
        }

        List<Long> groupIds = content.stream().map(MyGroupListRow::groupId).toList();

        Map<Long, List<String>> imageMap =
                groupV2QueryRepository.fetchMainImageUrlsByGroupIds(groupIds,
                        GROUP_LIST_IMAGE_LIMIT);
        Map<Long, List<String>> tagMap =
                groupV2QueryRepository.fetchTagNamesByGroupIds(groupIds);

        List<GetMyGroupListV2Response.Item> items = content.stream()
                .map(r -> {
                    int participantCount =
                            (r.participantCount() == null) ? 0 : r.participantCount().intValue();
                    int maxParticipants = (r.maxParticipants() == null) ? 0 : r.maxParticipants();

                    MyMembership myMembership = new MyMembership(
                            r.myStatus() == GroupUserV2Status.ATTEND,
                            r.myGroupUserId(),
                            r.myRole(),
                            r.myStatus(),
                            r.myJoinedAt(),
                            r.myLeftAt()
                    );

                    return GetMyGroupListV2Response.Item.of(
                            r.groupId(),
                            r.title(),
                            r.status(),
                            r.location(),
                            r.locationDetail(),
                            r.startTime(),
                            r.endTime(),
                            imageMap.getOrDefault(r.groupId(), List.of()),
                            tagMap.getOrDefault(r.groupId(), List.of()),
                            r.description(),
                            participantCount,
                            maxParticipants,
                            CreatedBy.of(
                                    r.hostId(),
                                    r.hostNickName(),
                                    r.hostProfileImage(),
                                    r.hostProfileMessage()
                            ),
                            r.createdAt(),
                            r.updatedAt(),
                            myMembership
                    );
                })
                .toList();

        return GetMyGroupListV2Response.of(items, nextCursor);
    }

    private ResolvedStatuses resolveStatuses(
            GroupListFilter filter,
            List<GroupV2Status> includeStatuses,
            List<GroupV2Status> excludeStatuses
    ) {
        boolean hasInclude = includeStatuses != null && !includeStatuses.isEmpty();
        boolean hasExclude = excludeStatuses != null && !excludeStatuses.isEmpty();

        List<GroupV2Status> includes = filter.defaultIncludeStatuses();
        List<GroupV2Status> excludes = filter.defaultExcludeStatuses();

        if (includes == null) {
            includes = List.of();
        }
        if (excludes == null) {
            excludes = List.of();
        }

        if (hasInclude) {
            includes = includeStatuses;
            excludes = List.of();
        }
        if (hasExclude) {
            excludes = excludeStatuses;
        }

        // include/exclude/filter 아무것도 안 왔고 ACTIVE면 기본 노출 세트
        if (!hasInclude && !hasExclude && filter != GroupListFilter.ALL && includes.isEmpty()) {
            includes = List.of(GroupV2Status.RECRUITING, GroupV2Status.FULL);
        }

        // 충돌 제거
        if (!includes.isEmpty() && !excludes.isEmpty()) {
            var includeSet = java.util.EnumSet.copyOf(includes);
            excludes.forEach(includeSet::remove);
            includes = List.copyOf(includeSet);
        }

        return new ResolvedStatuses(includes, excludes);
    }

    private record ResolvedStatuses(List<GroupV2Status> includes, List<GroupV2Status> excludes) {

    }
}

