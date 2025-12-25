package team.wego.wegobackend.group.v2.application.service;

import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.wego.wegobackend.group.domain.exception.GroupErrorCode;
import team.wego.wegobackend.group.domain.exception.GroupException;
import team.wego.wegobackend.group.v2.application.dto.common.PreUploadedGroupImage;
import team.wego.wegobackend.group.v2.application.dto.request.CreateGroupImageV2Request;
import team.wego.wegobackend.group.v2.application.dto.request.CreateGroupV2Request;
import team.wego.wegobackend.group.v2.application.dto.request.GroupListFilter;
import team.wego.wegobackend.group.v2.application.dto.response.CreateGroupV2Response;
import team.wego.wegobackend.group.v2.application.dto.response.GetGroupListV2Response;
import team.wego.wegobackend.group.v2.application.dto.response.GetGroupListV2Response.GroupListItemV2Response;
import team.wego.wegobackend.group.v2.application.dto.response.GetGroupListV2Response.GroupListItemV2Response.CreatedByV2Response;
import team.wego.wegobackend.group.v2.application.dto.response.GetGroupV2Response;
import team.wego.wegobackend.group.v2.application.event.GroupCreatedEvent;
import team.wego.wegobackend.group.v2.domain.entity.GroupImageV2;
import team.wego.wegobackend.group.v2.domain.entity.GroupTagV2;
import team.wego.wegobackend.group.v2.domain.entity.GroupUserV2;
import team.wego.wegobackend.group.v2.domain.entity.GroupUserV2Role;
import team.wego.wegobackend.group.v2.domain.entity.GroupV2;
import team.wego.wegobackend.group.v2.domain.entity.GroupV2Address;
import team.wego.wegobackend.group.v2.domain.entity.GroupV2Status;
import team.wego.wegobackend.group.v2.domain.repository.GroupImageV2Repository;
import team.wego.wegobackend.group.v2.domain.repository.GroupUserV2Repository;
import team.wego.wegobackend.group.v2.domain.repository.GroupV2QueryRepository;
import team.wego.wegobackend.group.v2.domain.repository.GroupV2Repository;
import team.wego.wegobackend.group.v2.infrastructure.querydsl.projection.GroupListRow;
import team.wego.wegobackend.group.v2.infrastructure.redis.PreUploadedGroupImageRedisRepository;
import team.wego.wegobackend.tag.application.service.TagService;
import team.wego.wegobackend.tag.domain.entity.Tag;
import team.wego.wegobackend.user.domain.User;
import team.wego.wegobackend.user.repository.UserRepository;

@Slf4j
@RequiredArgsConstructor
@Service
public class GroupV2Service {

    private final GroupImageV2Repository groupImageV2Repository;
    private final GroupUserV2Repository groupUserV2Repository;
    private final GroupV2QueryRepository groupV2QueryRepository;
    private final GroupV2Repository groupV2Repository;
    private final PreUploadedGroupImageRedisRepository preUploadedGroupImageRedisRepository;

    private final GroupCreateCooldownService groupCreateCooldownService;

    // 태그 호출
    private final TagService tagService;

    // 회원 호출
    private final UserRepository userRepository;

    // SSE 이벤트 호출
    private final ApplicationEventPublisher eventPublisher;


    private static final int MAX_PAGE_SIZE = 50;
    private static final int GROUP_LIST_IMAGE_LIMIT = 3;
    private static final int COOL_DOWN_SECONDS = 5;

    @Transactional(readOnly = true)
    public GetGroupListV2Response getGroupListV2(
            String keyword,
            Long cursor,
            int size,
            GroupListFilter filter,
            List<GroupV2Status> includeStatuses,
            List<GroupV2Status> excludeStatuses
    ) {
        // filter null 방어
        filter = (filter == null) ? GroupListFilter.ACTIVE : filter;

        int pageSize = Math.max(1, Math.min(size, MAX_PAGE_SIZE));

        boolean hasInclude = includeStatuses != null && !includeStatuses.isEmpty();
        boolean hasExclude = excludeStatuses != null && !excludeStatuses.isEmpty();

        // 여기서부터 includes/excludes는 "절대 null 아님" 규칙으로 설정
        List<GroupV2Status> includes = filter.defaultIncludeStatuses();
        List<GroupV2Status> excludes = filter.defaultExcludeStatuses();

        // GroupListFilter의 default가 절대 null 반환하지 않게 보장
        if (includes == null) {
            includes = List.of();
        }
        if (excludes == null) {
            excludes = List.of();
        }

        // include가 오면 include가 우선: filter include 무시
        if (hasInclude) {
            includes = includeStatuses;
            // include가 명시된 순간 filter의 exclude까지 섞이면 예측이 어려워져서 초기화
            excludes = List.of();
        }

        // exclude가 오면 exclude는 요청값으로 덮어쓰기
        if (hasExclude) {
            excludes = excludeStatuses;
        }

        // 안전장치: filter=ALL이면 "전체"가 맞으므로 기본값 강제 X
        // filter != ALL이고, 아무것도 안 왔고, includes가 비어있다면 ACTIVE 기본값 적용
        if (!hasInclude && !hasExclude && filter != GroupListFilter.ALL && includes.isEmpty()) {
            includes = List.of(GroupV2Status.RECRUITING, GroupV2Status.FULL);
        }

        // 4) 충돌 제거: exclude 우선한다. 람다 캡처 문제 없애기 위해 Set 연산으로 처리
        if (!includes.isEmpty() && !excludes.isEmpty()) {
            // includes가 "전체" 의미(빈 리스트)인 경우엔 여기 들어오지 않음
            var includeSet = java.util.EnumSet.copyOf(includes);
            excludes.forEach(includeSet::remove);
            includes = List.copyOf(includeSet); // 불변 리스트로 정리
        }

        List<GroupListRow> rows = groupV2QueryRepository.fetchGroupRows(
                keyword, cursor, pageSize + 1, includes, excludes
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
                .map(groupListRow -> {
                    int participantCount = (groupListRow.participantCount() == null) ? 0
                            : groupListRow.participantCount().intValue();
                    return GroupListItemV2Response.of(
                            groupListRow.groupId(),
                            groupListRow.title(),
                            groupListRow.joinPolicy(),
                            groupListRow.status(),
                            groupListRow.location(),
                            groupListRow.locationDetail(),
                            groupListRow.startTime(),
                            groupListRow.endTime(),
                            imageMap.getOrDefault(groupListRow.groupId(), List.of()),
                            tagMap.getOrDefault(groupListRow.groupId(), List.of()),
                            groupListRow.description(),
                            participantCount,
                            groupListRow.maxParticipants(),
                            CreatedByV2Response.of(
                                    groupListRow.hostId(),
                                    groupListRow.hostNickName(),
                                    groupListRow.hostProfileImage()
                            ),
                            groupListRow.createdAt(),
                            groupListRow.updatedAt()
                    );
                })
                .toList();

        return GetGroupListV2Response.of(items, nextCursor);
    }


    @Transactional
    public CreateGroupV2Response create(Long userId, CreateGroupV2Request request) {
        if (userId == null) {
            throw new GroupException(GroupErrorCode.USER_ID_NULL);
        }

        groupCreateCooldownService.acquireOrThrowWithRollbackRelease(userId, COOL_DOWN_SECONDS);

        // 회원 조회
        User host = userRepository.findById(userId)
                .orElseThrow(() -> new GroupException(GroupErrorCode.HOST_USER_NOT_FOUND, userId)
                );

        // 모임 주소 생성
        GroupV2Address address = GroupV2Address.of(request.location(), request.locationDetail());

        // 모임 생성
        GroupV2 group = GroupV2.create(
                request.title(),
                address,
                request.startTime(),
                request.endTime(),
                request.description(),
                request.maxParticipants(),
                host,
                request.joinPolicy()
        );

        // 모임 주최자 생성
        GroupUserV2.create(group, host, GroupUserV2Role.HOST);

        // 태그 생성 또는 찾기
        if (request.tags() != null) {
            List<Tag> tags = tagService.findOrCreateAll(request.tags());
            for (Tag tag : tags) {
                GroupTagV2.create(group, tag);
            }
        }

        // 이미지 생성
        if (request.images() != null && !request.images().isEmpty()) {
            for (int i = 0; i < request.images().size(); i++) {
                CreateGroupImageV2Request imgReq = request.images().get(i);

                String imageKey = imgReq.imageKey();
                if (imageKey == null || imageKey.isBlank()) {
                    throw new GroupException(GroupErrorCode.GROUP_IMAGE_KEY_REQUIRED);
                }

                int sortOrder = (imgReq.sortOrder() != null) ? imgReq.sortOrder() : i;

                PreUploadedGroupImage pre = preUploadedGroupImageRedisRepository.consume(imageKey)
                        .orElseThrow(() -> new GroupException(
                                GroupErrorCode.PRE_UPLOADED_IMAGE_NOT_FOUND, imageKey
                        ));

                if (!userId.equals(pre.uploaderId())) {
                    throw new GroupException(GroupErrorCode.PRE_UPLOADED_IMAGE_OWNER_MISMATCH,
                            imageKey);
                }

                GroupImageV2.create(
                        group,
                        sortOrder,
                        pre.imageKey(),
                        pre.url440x240(),
                        pre.url100x100()
                );
            }
        }

        // 모임 저장
        GroupV2 saved = groupV2Repository.save(group);

        // 이벤트 발행
        log.info("[GROUP] created. groupId={}, hostId={}", saved.getId(), host.getId());
        eventPublisher.publishEvent(new GroupCreatedEvent(saved.getId(), host.getId()));
        log.info("[GROUP] published GroupCreatedEvent. groupId={}, hostId={}", saved.getId(), host.getId());


        return CreateGroupV2Response.from(saved, host);
    }

    @Transactional(readOnly = true)
    public GetGroupV2Response getGroup(Long userId, Long groupId) {
        GroupV2 group = groupV2Repository.findGroupWithHostAndTags(groupId)
                .orElseThrow(
                        () -> new GroupException(GroupErrorCode.GROUP_NOT_FOUND_BY_ID, groupId));

        // 컬렉션은 안전하게 따로 보관해서 옮겨야 좋다고 한다.
        List<GroupImageV2> images = groupImageV2Repository.findAllByGroupIdWithVariants(groupId);
        List<GroupUserV2> users = groupUserV2Repository.findAllByGroupIdWithUser(groupId);

        return GetGroupV2Response.of(group, images, users, userId);
    }

}
