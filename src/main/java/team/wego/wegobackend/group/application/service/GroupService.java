package team.wego.wegobackend.group.application.service;

import static team.wego.wegobackend.group.domain.entity.GroupUserStatus.ATTEND;
import static team.wego.wegobackend.group.domain.entity.GroupUserStatus.LEFT;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.wego.wegobackend.group.application.dto.request.CreateGroupImageRequest;
import team.wego.wegobackend.group.application.dto.request.CreateGroupRequest;
import team.wego.wegobackend.group.application.dto.request.UpdateGroupRequest;
import team.wego.wegobackend.group.application.dto.response.CreateGroupResponse;
import team.wego.wegobackend.group.application.dto.response.GetGroupListResponse;
import team.wego.wegobackend.group.application.dto.response.GetGroupResponse;
import team.wego.wegobackend.group.application.dto.response.GetGroupResponse.CreatedByResponse;
import team.wego.wegobackend.group.application.dto.response.GetGroupResponse.JoinedMemberResponse;
import team.wego.wegobackend.group.application.dto.response.GetGroupResponse.UserStatusResponse;
import team.wego.wegobackend.group.application.dto.response.GroupImageItemResponse;
import team.wego.wegobackend.group.application.dto.response.GroupListItemResponse;
import team.wego.wegobackend.group.domain.entity.Group;
import team.wego.wegobackend.group.domain.entity.GroupImage;
import team.wego.wegobackend.group.domain.entity.GroupRole;
import team.wego.wegobackend.group.domain.entity.GroupTag;
import team.wego.wegobackend.group.domain.entity.GroupUser;
import team.wego.wegobackend.group.domain.exception.GroupErrorCode;
import team.wego.wegobackend.group.domain.exception.GroupException;
import team.wego.wegobackend.group.domain.repository.GroupImageRepository;
import team.wego.wegobackend.group.domain.repository.GroupRepository;
import team.wego.wegobackend.group.domain.repository.GroupTagRepository;
import team.wego.wegobackend.group.domain.repository.GroupUserRepository;
import team.wego.wegobackend.image.application.service.ImageUploadService;
import team.wego.wegobackend.tag.domain.entity.Tag;
import team.wego.wegobackend.tag.domain.repository.TagRepository;
import team.wego.wegobackend.user.domain.User;
import team.wego.wegobackend.user.repository.UserRepository;

@RequiredArgsConstructor
@Service
public class GroupService {

    private final UserRepository userRepository;
    private final TagRepository tagRepository;

    private final GroupRepository groupRepository;
    private final GroupImageRepository groupImageRepository;
    private final GroupTagRepository groupTagRepository;
    private final GroupUserRepository groupUserRepository;

    private final ImageUploadService imageUploadService;

    private void validateCreateGroupRequest(CreateGroupRequest request) {
        if (request.endTime() != null && !request.endTime().isAfter(request.startTime())) {
            throw new GroupException(GroupErrorCode.INVALID_TIME_RANGE);
        }

        if (request.maxParticipants() == null || request.maxParticipants() < 2) {
            throw new GroupException(GroupErrorCode.INVALID_MAX_PARTICIPANTS);
        }
    }

    private void saveGroupTags(Group group, List<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty()) {
            return;
        }

        // 1. null, 공백 제거 + trim + 중복 제거
        List<String> normalized = tagNames.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(name -> !name.isBlank())
                .distinct()
                .toList();

        if (normalized.isEmpty()) {
            return;
        }

        // 2. 이미 존재하는 태그 조회
        List<Tag> existingTags = tagRepository.findByNameIn(normalized);

        Map<String, Tag> tagByName = existingTags.stream()
                .collect(Collectors.toMap(Tag::getName, Function.identity()));

        // 3. 없는 태그는 새로 생성
        List<Tag> newTags = normalized.stream()
                .filter(name -> !tagByName.containsKey(name))
                .map(Tag::create)
                .toList();

        if (!newTags.isEmpty()) {
            List<Tag> saved = tagRepository.saveAll(newTags);
            // 새로 생성된 태그도 map에 추가
            saved.forEach(tag -> tagByName.put(tag.getName(), tag));
        }

        // 4. GroupTag 저장 (normalized 순서대로)
        List<GroupTag> groupTags = normalized.stream()
                .map(name -> {
                    Tag tag = tagByName.get(name); // 무조건 존재
                    return GroupTag.create(group, tag);
                })
                .toList();

        groupTagRepository.saveAll(groupTags);
    }

    private void saveHostAsGroupUser(Group group, User host) {
        GroupUser groupUser = GroupUser.create(group, host, GroupRole.HOST);
        groupUserRepository.save(groupUser);
    }

    @Transactional
    public CreateGroupResponse createGroup(Long hostUserId, CreateGroupRequest request) {
        // 0. 기본 검증
        validateCreateGroupRequest(request);

        // 1. HOST 조회
        User host = userRepository.findById(hostUserId)
                .orElseThrow(
                        () -> new GroupException(GroupErrorCode.HOST_USER_NOT_FOUND, hostUserId));

        // 2. Group 생성 및 저장
        Group group = Group.create(
                request.title(),
                request.location(),
                request.locationDetail(),
                request.startTime(),
                request.endTime(),
                request.description(),
                request.maxParticipants(),
                host
        );
        groupRepository.save(group);

        // 3. HOST를 GroupUser(HOST)로 저장
        saveHostAsGroupUser(group, host);

        // 4. 태그 저장 (이름 기반)
        saveGroupTags(group, request.tags());

        // 5. 이미지 URL 기반으로 GroupImage 생성 + 응답 DTO 구성
        List<GroupImageItemResponse> imageResponses = saveGroupImagesByUrl(group, request.images());

        // 6. 응답 생성 (그룹 + 이미지 모두 포함)
        return CreateGroupResponse.from(group, imageResponses);
    }

    /**
     * CreateGroupRequest.images 안에 들어있는 imageUrl440x240 / imageUrl100x100 을 사용해서 GroupImage 엔티티를
     * 생성하고 저장한 뒤, 응답 DTO로 변환합니다.
     */
    private List<GroupImageItemResponse> saveGroupImagesByUrl(
            Group group,
            List<CreateGroupImageRequest> imageRequests
    ) {
        if (imageRequests == null || imageRequests.isEmpty()) {
            return List.of();
        }

        // sortOrder 기준 정렬
        List<CreateGroupImageRequest> sorted = imageRequests.stream()
                .filter(Objects::nonNull)
                .sorted((a, b) -> {
                    int s1 = a.sortOrder() != null ? a.sortOrder() : 0;
                    int s2 = b.sortOrder() != null ? b.sortOrder() : 0;
                    return Integer.compare(s1, s2);
                })
                .toList();

        // 엔티티 2배 생성 (한 이미지는 440, 100 두 행)
        List<GroupImage> entities = new java.util.ArrayList<>();

        for (CreateGroupImageRequest req : sorted) {
            int sortOrder = req.sortOrder() != null ? req.sortOrder() : 0;

            // MAIN: 440x240
            if (req.imageUrl440x240() != null && !req.imageUrl440x240().isBlank()) {
                GroupImage main = GroupImage.create(group, req.imageUrl440x240(), sortOrder);
                entities.add(main);
            }

            // THUMB: 100x100
            if (req.imageUrl100x100() != null && !req.imageUrl100x100().isBlank()) {
                GroupImage thumb = GroupImage.create(group, req.imageUrl100x100(), sortOrder);
                entities.add(thumb);
            }
        }

        if (!entities.isEmpty()) {
            groupImageRepository.saveAll(entities);
        }

        // sortOrder 기준으로 묶어서 "논리적인 한 장" 단위로 응답 DTO 생성
        Map<Integer, List<GroupImage>> byOrder = entities.stream()
                .collect(Collectors.groupingBy(GroupImage::getSortOrder));

        return byOrder.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())  // sortOrder 오름차순
                .map(entry -> {
                    int sortOrder = entry.getKey();
                    List<GroupImage> list = entry.getValue();

                    // 440x240 행
                    GroupImage main = list.stream()
                            .filter(img -> img.getImageUrl().contains("440x240"))
                            .findFirst()
                            .orElse(list.getFirst()); // 혹시 패턴이 안 맞으면 첫 번째를 대표로

                    // 100x100 행
                    GroupImage thumb = list.stream()
                            .filter(img -> img.getImageUrl().contains("100x100"))
                            .findFirst()
                            .orElse(null);

                    return GroupImageItemResponse.from(main, thumb);

                })
                .toList();
    }

    @Transactional
    public GetGroupResponse attendGroup(Long groupId, Long memberId) {
        // 0. Group 조회 (삭제된 모임 제외)
        Group group = groupRepository.findByIdAndDeletedAtIsNull(groupId)
                .orElseThrow(
                        () -> new GroupException(GroupErrorCode.GROUP_NOT_FOUND_BY_ID, groupId));

        // 1. member 조회
        User member = userRepository.findById(memberId)
                .orElseThrow(() -> new GroupException(GroupErrorCode.MEMBER_NOT_FOUND, memberId));

        // 2. 이미 GroupUser row가 있는지 확인 (한 모임/유저당 한 row 가정)
        GroupUser groupUser = groupUserRepository.findByGroupAndUser(group, member)
                .orElse(null);

        if (groupUser != null && groupUser.getStatus() == ATTEND) {
            // 이미 참여 중인 경우
            throw new GroupException(GroupErrorCode.ALREADY_ATTEND_GROUP, groupId, memberId);
        }

        // 3. 정원 체크 (ATTEND 상태인 인원 수 기준)
        long currentAttendCount = groupUserRepository.countByGroupAndStatus(group, ATTEND);
        if (currentAttendCount >= group.getMaxParticipants()) {
            throw new GroupException(GroupErrorCode.GROUP_CAPACITY_EXCEEDED, groupId);
        }

        // 4. 참여 처리
        if (groupUser == null) {
            // 처음 참여
            GroupUser newGroupUser = GroupUser.create(group, member, GroupRole.MEMBER);
            groupUserRepository.save(newGroupUser);
        } else if (groupUser.getStatus() == LEFT) {
            // 이전에 나갔다가 다시 참여
            groupUser.reAttend();
            // dirty checking 으로 업데이트
        }

        return buildGetGroupResponse(group, memberId);
    }

    @Transactional
    public GetGroupResponse cancelAttendGroup(Long groupId, Long memberId) {
        // 0. Group 조회 (soft delete 고려)
        Group group = groupRepository.findByIdAndDeletedAtIsNull(groupId)
                .orElseThrow(
                        () -> new GroupException(GroupErrorCode.GROUP_NOT_FOUND_BY_ID, groupId));

        // 1. member 조회
        User member = userRepository.findById(memberId)
                .orElseThrow(() -> new GroupException(GroupErrorCode.MEMBER_NOT_FOUND, memberId));

        // 2. GroupUser 찾기
        GroupUser groupUser = groupUserRepository.findByGroupAndUser(group, member)
                .orElseThrow(() -> new GroupException(GroupErrorCode.NOT_ATTEND_GROUP, groupId,
                        memberId));

        // 3. HOST는 나갈 수 없음
        if (groupUser.getGroupRole() == GroupRole.HOST) {
            throw new GroupException(GroupErrorCode.HOST_CANNOT_LEAVE_OWN_GROUP, groupId, memberId);
        }

        // 4. 이미 나간 상태면 예외
        if (groupUser.getStatus() == LEFT) {
            throw new GroupException(GroupErrorCode.NOT_ATTEND_GROUP, groupId, memberId);
        }

        // 5. 참여 취소 (leave)
        groupUser.leave();

        // 6. 최신 모임 상세 응답 반환
        return buildGetGroupResponse(group, memberId);
    }

    @Transactional(readOnly = true)
    public GetGroupListResponse getGroupList(String keyword, Long cursor, int size) {

        // 1. size 기본 방어 로직 (1 ~ 50 사이로 제한)
        int pageSize = Math.max(1, Math.min(size, 50));

        // 2. keyword가 비었으면 null로 통일해서 쿼리 단에서 처리
        String normalizedKeyword = (keyword == null || keyword.isBlank()) ? null : keyword;

        // 3. limit = pageSize + 1: 다음 페이지 여부 판단용
        List<Group> groups = groupRepository.findGroupsWithKeywordAndCursor(
                normalizedKeyword,
                cursor,
                pageSize + 1
        );

        Long nextCursor = null;
        if (groups.size() > pageSize) {
            Group lastExtra = groups.remove(pageSize); // size+1 중 마지막 하나 제거
            nextCursor = lastExtra.getId();           // 그 id를 다음 커서로 사용
        }

        List<GroupListItemResponse> items = groups.stream()
                .map(this::toGroupListItemResponse)
                .toList();

        return GetGroupListResponse.of(items, nextCursor);
    }

    private GroupListItemResponse toGroupListItemResponse(Group group) {
        // sortOrder 기준으로 묶어서 대표(440x240)만 선택
        Map<Integer, List<GroupImage>> byOrder = group.getImages().stream()
                .collect(Collectors.groupingBy(GroupImage::getSortOrder));

        List<String> imageUrls = byOrder.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .limit(3) // 논리적인 3장까지만
                .map(entry -> {
                    List<GroupImage> list = entry.getValue();
                    return list.stream()
                            .filter(img -> img.getImageUrl().contains("440x240"))
                            .findFirst()
                            .orElse(list.get(0))
                            .getImageUrl();
                })
                .toList();

        List<String> tagNames = group.getGroupTags().stream()
                .map(GroupTag::getTag)
                .map(Tag::getName)
                .toList();

        int participantCount = (int) group.getUsers().stream()
                .filter(gu -> gu.getStatus() == ATTEND)
                .count();

        CreatedByResponse createdBy = CreatedByResponse.from(group.getHost());

        return GroupListItemResponse.of(
                group,
                imageUrls,
                tagNames,
                participantCount,
                createdBy
        );
    }


    private GetGroupResponse buildGetGroupResponse(Group group, Long currentUserId) {
        // 1) 이미지: sortOrder 기준으로 묶어서 440 + 100 세트 만들기
        Map<Integer, List<GroupImage>> byOrder = group.getImages().stream()
                .collect(Collectors.groupingBy(GroupImage::getSortOrder));

        List<GroupImageItemResponse> images = byOrder.entrySet().stream()
                .sorted(Map.Entry.comparingByKey()) // sortOrder 기준
                .map(entry -> {
                    int sortOrder = entry.getKey();
                    List<GroupImage> list = entry.getValue();

                    GroupImage main = list.stream()
                            .filter(img -> img.getImageUrl().contains("440x240"))
                            .findFirst()
                            .orElse(list.getFirst());

                    GroupImage thumb = list.stream()
                            .filter(img -> img.getImageUrl().contains("100x100"))
                            .findFirst()
                            .orElse(null);

                    return GroupImageItemResponse.from(main, thumb);
                })
                .toList();

        // 2) 태그 이름
        List<String> tagNames = group.getGroupTags().stream()
                .map(GroupTag::getTag)
                .map(Tag::getName)
                .toList();

        // 3) 현재 참여 인원 수 (status == ATTEND 인 인원만)
        int participantCount = (int) group.getUsers().stream()
                .filter(gu -> gu.getStatus() == ATTEND)
                .count();

        // 4) 만든 사람 정보
        CreatedByResponse createdBy = CreatedByResponse.from(group.getHost());

        // 5) 현재 요청 유저의 참여 상태 (currentUserId가 null일 수도 있음)
        UserStatusResponse userStatus;
        if (currentUserId == null) {
            userStatus = UserStatusResponse.notJoined();
        } else {
            userStatus = group.getUsers().stream()
                    .filter(gu -> gu.getUser().getId().equals(currentUserId))
                    .filter(gu -> gu.getStatus() == ATTEND)
                    .findFirst()
                    .map(gu -> UserStatusResponse.fromJoined(gu.getJoinedAt()))
                    .orElse(UserStatusResponse.notJoined());
        }

        // 6) 참여 중인 멤버 리스트 (status == ATTEND)
        List<JoinedMemberResponse> joinedMembers = group.getUsers().stream()
                .filter(gu -> gu.getStatus() == ATTEND)
                .sorted(Comparator.comparing(GroupUser::getJoinedAt))
                .map(JoinedMemberResponse::from)
                .toList();

        return GetGroupResponse.of(
                group,
                images,
                tagNames,
                participantCount,
                createdBy,
                userStatus,
                joinedMembers
        );
    }

    private String resolveThumbnailUrl(String mainUrl) {
        if (mainUrl == null || mainUrl.isBlank()) {
            return null;
        }

        if (mainUrl.contains("440x240")) {
            return mainUrl.replace("440x240", "100x100");
        }

        return mainUrl;
    }

    @Transactional(readOnly = true)
    public GetGroupResponse getGroup(Long groupId, Long currentUserId) {
        Group group = groupRepository.findByIdAndDeletedAtIsNull(groupId)
                .orElseThrow(
                        () -> new GroupException(GroupErrorCode.GROUP_NOT_FOUND_BY_ID, groupId));

        return buildGetGroupResponse(group, currentUserId);
    }

    @Transactional(readOnly = true)
    public GetGroupResponse getGroup(Long groupId) {
        // 익명 / 비로그인 / userId 모르는 경우
        Group group = groupRepository.findByIdAndDeletedAtIsNull(groupId)
                .orElseThrow(
                        () -> new GroupException(GroupErrorCode.GROUP_NOT_FOUND_BY_ID, groupId));

        // currentUserId = null로 넘기면 userStatus.notJoined()
        return buildGetGroupResponse(group, null);
    }


    private void updateGroupTags(Group group, List<String> tagNames) {
        if (tagNames == null) { // null이면 "태그는 건드리지 않는다"는 의미로 처리
            return;
        }
        group.getGroupTags().clear();
        saveGroupTags(group, tagNames);
    }

    private void validateUpdateGroupRequest(UpdateGroupRequest request, int currentAttendCount) {
        // 모임 시간 검증: 둘 다 있을 때만 비교
        if (request.startTime() != null
                && request.endTime() != null
                && !request.endTime().isAfter(request.startTime())) {
            throw new GroupException(GroupErrorCode.INVALID_TIME_RANGE);
        }

        // 최소 인원 검증
        if (request.maxParticipants() == null || request.maxParticipants() < 2) {
            throw new GroupException(GroupErrorCode.INVALID_MAX_PARTICIPANTS);
        }

        // 현재 참여 인원보다 작게 줄일 수 없음
        if (request.maxParticipants() < currentAttendCount) {
            throw new GroupException(
                    GroupErrorCode.INVALID_MAX_PARTICIPANTS_LESS_THAN_CURRENT,
                    currentAttendCount
            );
        }
    }

    @Transactional
    public GetGroupResponse updateGroup(Long userId, Long groupId, UpdateGroupRequest request) {
        // 1. 모임 조회
        Group group = groupRepository.findByIdAndDeletedAtIsNull(groupId)
                .orElseThrow(
                        () -> new GroupException(GroupErrorCode.GROUP_NOT_FOUND_BY_ID, groupId));

        // 2. 권한 체크: HOST만 수정 가능
        if (!group.getHost().getId().equals(userId)) {
            throw new GroupException(
                    GroupErrorCode.NO_PERMISSION_TO_UPDATE_GROUP,
                    groupId,
                    userId
            );
        }

        // 3. 현재 ATTEND 인원 수 (HOST 포함)
        int currentAttendCount = (int) group.getUsers().stream()
                .filter(gu -> gu.getStatus() == ATTEND)
                .count();

        // 4. 요청 값 검증 (시간 / 최대 인원 / 현재 인원 대비)
        validateUpdateGroupRequest(request, currentAttendCount);

        // 5. 엔티티 필드 수정 (이미지는 건드리지 않음)
        group.update(
                request.title(),
                request.location(),
                request.locationDetail(),
                request.startTime(),
                request.endTime(),
                request.description(),
                request.maxParticipants()
        );

        // 6. 태그 수정
        updateGroupTags(group, request.tags());

        // 7. 수정 결과 응답 (현재 유저 기준)
        return buildGetGroupResponse(group, userId);
    }

    @Transactional
    public void deleteGroup(Long userId, Long groupId) {
        // 1. 모임 조회 (soft delete 고려 쿼리 사용 중이지만, 여기서는 실제 삭제)
        Group group = groupRepository.findByIdAndDeletedAtIsNull(groupId)
                .orElseThrow(
                        () -> new GroupException(GroupErrorCode.GROUP_NOT_FOUND_BY_ID, groupId)
                );

        // 2. HOST 권한 체크
        if (!group.getHost().getId().equals(userId)) {
            throw new GroupException(
                    GroupErrorCode.NO_PERMISSION_TO_UPDATE_GROUP, // 삭제도 동일 코드 재사용
                    groupId,
                    userId
            );
        }

        // 3. S3 이미지 삭제 (이미지 URL -> key 변환은 ImageUploadService 가 담당)
        List<String> imageUrls = group.getImages().stream()
                .map(GroupImage::getImageUrl)
                .filter(Objects::nonNull)
                .toList();

        imageUploadService.deleteAllByUrls(imageUrls);

        groupRepository.delete(group);
    }
}
