package team.wego.wegobackend.group.v2.application.service;

import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.wego.wegobackend.group.domain.exception.GroupErrorCode;
import team.wego.wegobackend.group.domain.exception.GroupException;
import team.wego.wegobackend.group.v2.application.dto.common.MyMembership;
import team.wego.wegobackend.group.v2.application.dto.request.CreateGroupImageV2Request;
import team.wego.wegobackend.group.v2.application.dto.request.CreateGroupV2Request;
import team.wego.wegobackend.group.v2.application.dto.response.AttendGroupV2Response;
import team.wego.wegobackend.group.v2.application.dto.response.CreateGroupV2Response;
import team.wego.wegobackend.group.v2.application.dto.response.GetGroupListV2Response;
import team.wego.wegobackend.group.v2.application.dto.response.GetGroupListV2Response.GroupListItemV2Response;
import team.wego.wegobackend.group.v2.application.dto.response.GetGroupListV2Response.GroupListItemV2Response.CreatedByV2Response;
import team.wego.wegobackend.group.v2.application.dto.response.GetGroupV2Response;
import team.wego.wegobackend.group.v2.domain.entity.GroupImageV2;
import team.wego.wegobackend.group.v2.domain.entity.GroupTagV2;
import team.wego.wegobackend.group.v2.domain.entity.GroupUserV2;
import team.wego.wegobackend.group.v2.domain.entity.GroupUserV2Role;
import team.wego.wegobackend.group.v2.domain.entity.GroupUserV2Status;
import team.wego.wegobackend.group.v2.domain.entity.GroupV2;
import team.wego.wegobackend.group.v2.domain.entity.GroupV2Address;
import team.wego.wegobackend.group.v2.domain.entity.GroupV2Status;
import team.wego.wegobackend.group.v2.domain.repository.GroupImageV2Repository;
import team.wego.wegobackend.group.v2.domain.repository.GroupUserV2Repository;
import team.wego.wegobackend.group.v2.domain.repository.GroupV2QueryRepository;
import team.wego.wegobackend.group.v2.domain.repository.GroupV2Repository;
import team.wego.wegobackend.group.v2.infrastructure.querydsl.projection.GroupListRow;
import team.wego.wegobackend.tag.application.service.TagService;
import team.wego.wegobackend.tag.domain.entity.Tag;
import team.wego.wegobackend.user.domain.User;
import team.wego.wegobackend.user.repository.UserRepository;

@RequiredArgsConstructor
@Service
public class GroupV2Service {

    private final GroupImageV2Repository groupImageV2Repository;
    private final GroupUserV2Repository groupUserV2Repository;
    private final GroupV2QueryRepository groupV2QueryRepository;
    private final GroupV2Repository groupV2Repository;

    // 태그 호출
    private final TagService tagService;

    // 회원 호출
    private final UserRepository userRepository;


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

    @Transactional
    public CreateGroupV2Response create(Long userId,
            CreateGroupV2Request request) {
        if (userId == null) {
            throw new GroupException(GroupErrorCode.USER_ID_NULL);
        }

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
                host
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
        if (request.images() != null) {
            for (CreateGroupImageV2Request imageRequest : request.images()) {
                GroupImageV2.create(group, imageRequest.sortOrder(), imageRequest.imageUrl440x240(),
                        imageRequest.imageUrl100x100());
            }
        }

        // 모임 저장
        GroupV2 saved = groupV2Repository.save(group);

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


    @Transactional
    public AttendGroupV2Response attend(Long userId, Long groupId) {
        if (userId == null) {
            throw new GroupException(GroupErrorCode.USER_ID_NULL);
        }

        GroupV2 group = groupV2Repository.findById(groupId)
                .orElseThrow(
                        () -> new GroupException(GroupErrorCode.GROUP_NOT_FOUND_BY_ID, groupId));

        if (group.getHost().getId().equals(userId)) {
            throw new GroupException(GroupErrorCode.GROUP_HOST_CANNOT_ATTEND);
        }

        // 모임 상태 체크
        if (group.getStatus() != GroupV2Status.RECRUITING) {
            throw new GroupException(GroupErrorCode.GROUP_NOT_RECRUITING, group.getStatus().name());
        }

        // 기존 멤버십 조회
        GroupUserV2 groupUserV2 = groupUserV2Repository.findByGroupIdAndUserId(groupId, userId)
                .orElse(null);

        if (groupUserV2 != null) {
            if (groupUserV2.getStatus() == GroupUserV2Status.BANNED) {
                throw new GroupException(GroupErrorCode.GROUP_BANNED_USER);
            }
            // 이미 참석중이면 충돌
            if (groupUserV2.getStatus() == GroupUserV2Status.ATTEND) {
                throw new GroupException(GroupErrorCode.ALREADY_ATTEND_GROUP, groupId, userId);
            }
            // LEFT/KICKED면 재참여 허용
            groupUserV2.reAttend();
        } else {
            // 최초 참석 생성
            groupUserV2 = GroupUserV2.create(group, userRepository.getReferenceById(userId),
                    GroupUserV2Role.MEMBER);
            // create에서 group.addUser로 연관관계 맞추는 구조.
            // group은 영속 상태여야 한다. (위에서 findById 했으니 ok)
            groupUserV2Repository.save(groupUserV2);
        }

        // 5) 정원 체크 수행. 재참여 포함해서 체크하는 게 안전
        long attendCount = groupUserV2Repository.countByGroupIdAndStatus(groupId,
                GroupUserV2Status.ATTEND);
        if (attendCount > group.getMaxParticipants()) {
            // 방금 reAttend로 늘었는데 초과하면 롤백시키기 위해 예외
            throw new GroupException(GroupErrorCode.GROUP_IS_FULL, groupId);
        }

        // FULL 자동 전환
        if (attendCount == group.getMaxParticipants()
                && group.getStatus() == GroupV2Status.RECRUITING) {
            group.changeStatus(GroupV2Status.FULL);
        }

        // 내 멤버십 + 최신 카운트 + 모임 상태 응답
        MyMembership membership =
                MyMembership.from(List.of(groupUserV2), userId);

        return AttendGroupV2Response.of(group, attendCount, membership);
    }

    @Transactional
    public AttendGroupV2Response left(Long userId, Long groupId) {
        if (userId == null) {
            throw new GroupException(GroupErrorCode.USER_ID_NULL);
        }

        GroupV2 group = groupV2Repository.findById(groupId)
                .orElseThrow(
                        () -> new GroupException(GroupErrorCode.GROUP_NOT_FOUND_BY_ID, groupId));

        if (group.getHost().getId().equals(userId)) {
            throw new GroupException(GroupErrorCode.GROUP_HOST_CANNOT_LEAVE, groupId, userId);
        }

        GroupUserV2 groupUserV2 = groupUserV2Repository.findByGroupIdAndUserId(groupId, userId)
                .orElseThrow(() -> new GroupException(GroupErrorCode.GROUP_USER_NOT_FOUND, userId));

        // ATTEND만 LEFT 가능
        if (groupUserV2.getStatus() != GroupUserV2Status.ATTEND) {
            throw new GroupException(GroupErrorCode.GROUP_NOT_ATTEND_STATUS);
        }

        groupUserV2.leave();

        long attendCount = groupUserV2Repository.countByGroupIdAndStatus(groupId,
                GroupUserV2Status.ATTEND);

        // FULL -> RECRUITING 자동 복귀(선택)
        if (group.getStatus() == GroupV2Status.FULL && attendCount < group.getMaxParticipants()) {
            group.changeStatus(GroupV2Status.RECRUITING);
        }

        // 응답
        MyMembership membership =
                MyMembership.from(List.of(groupUserV2), userId);

        return AttendGroupV2Response.of(group, attendCount, membership);
    }

}
