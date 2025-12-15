package team.wego.wegobackend.group.application.service.v1;

import static team.wego.wegobackend.group.domain.entity.GroupUserStatus.ATTEND;
import static team.wego.wegobackend.group.domain.entity.GroupUserStatus.LEFT;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.wego.wegobackend.common.security.CustomUserDetails;
import team.wego.wegobackend.group.application.dto.v1.request.CreateGroupImageRequest;
import team.wego.wegobackend.group.application.dto.v1.request.CreateGroupRequest;
import team.wego.wegobackend.group.application.dto.v1.request.UpdateGroupRequest;
import team.wego.wegobackend.group.application.dto.v1.response.CreateGroupResponse;
import team.wego.wegobackend.group.application.dto.v1.response.GetGroupListResponse;
import team.wego.wegobackend.group.application.dto.v1.response.GetGroupResponse;
import team.wego.wegobackend.group.application.dto.v1.response.GetGroupResponse.CreatedByResponse;
import team.wego.wegobackend.group.application.dto.v1.response.GetGroupResponse.JoinedMemberResponse;
import team.wego.wegobackend.group.application.dto.v1.response.GetGroupResponse.UserStatusResponse;
import team.wego.wegobackend.group.application.dto.v1.response.GroupImageItemResponse;
import team.wego.wegobackend.group.application.dto.v1.response.GroupListItemResponse;
import team.wego.wegobackend.group.domain.entity.Group;
import team.wego.wegobackend.group.domain.entity.GroupImage;
import team.wego.wegobackend.group.domain.entity.GroupRole;
import team.wego.wegobackend.group.domain.entity.GroupTag;
import team.wego.wegobackend.group.domain.entity.GroupUser;
import team.wego.wegobackend.group.domain.entity.MyGroupType;
import team.wego.wegobackend.group.domain.exception.GroupErrorCode;
import team.wego.wegobackend.group.domain.exception.GroupException;
import team.wego.wegobackend.group.domain.repository.v1.GroupImageRepository;
import team.wego.wegobackend.group.domain.repository.v1.GroupRepository;
import team.wego.wegobackend.group.domain.repository.v1.GroupTagRepository;
import team.wego.wegobackend.group.domain.repository.v1.GroupUserRepository;
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

    private static final int GROUP_LIST_IMAGE_LIMIT = 3;
    private static final int MAX_PAGE_SIZE = 50;

    private static final String MAIN_IMAGE_SIZE_TOKEN = "440x240";
    private static final String THUMB_IMAGE_SIZE_TOKEN = "100x100";

    /* =========================
     * Create
     * ========================= */

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

        List<String> normalized = tagNames.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(name -> !name.isBlank())
                .distinct()
                .toList();

        if (normalized.isEmpty()) {
            return;
        }

        List<Tag> existingTags = tagRepository.findByNameIn(normalized);
        Map<String, Tag> tagByName = existingTags.stream()
                .collect(Collectors.toMap(Tag::getName, Function.identity()));

        List<Tag> newTags = normalized.stream()
                .filter(name -> !tagByName.containsKey(name))
                .map(Tag::create)
                .toList();

        if (!newTags.isEmpty()) {
            List<Tag> saved = tagRepository.saveAll(newTags);
            saved.forEach(tag -> tagByName.put(tag.getName(), tag));
        }

        List<GroupTag> groupTags = normalized.stream()
                .map(name -> GroupTag.create(group, tagByName.get(name)))
                .toList();

        groupTagRepository.saveAll(groupTags);
    }

    private void registerHost(Group group, User host) {
        GroupUser groupUser = GroupUser.create(group, host, GroupRole.HOST);
        groupUserRepository.save(groupUser);

        host.increaseGroupJoinedCount();
        host.increaseGroupCreatedCount();
    }

    private User findUserByUserId(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new GroupException(GroupErrorCode.HOST_USER_NOT_FOUND, userId));
    }

    @Transactional
    public CreateGroupResponse createGroup(CustomUserDetails userDetails,
            CreateGroupRequest request) {
        Long userId = userDetails.getId();

        validateCreateGroupRequest(request);

        User host = findUserByUserId(userId);

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

        registerHost(group, host);

        saveGroupTags(group, request.tags());

        List<GroupImageItemResponse> imageResponses = saveGroupImagesByUrl(group, request.images());

        return CreateGroupResponse.from(group, imageResponses);
    }

    private List<GroupImageItemResponse> saveGroupImagesByUrl(Group group,
            List<CreateGroupImageRequest> imageRequests) {
        if (imageRequests == null || imageRequests.isEmpty()) {
            return List.of();
        }

        List<CreateGroupImageRequest> sorted = imageRequests.stream()
                .filter(Objects::nonNull)
                .sorted(Comparator.comparingInt(req -> sortOrderOrZero(req.sortOrder())))
                .toList();

        List<GroupImage> entities = new java.util.ArrayList<>();

        for (CreateGroupImageRequest req : sorted) {
            int sortOrder = req.sortOrder() != null ? req.sortOrder() : 0;

            if (req.imageUrl440x240() != null && !req.imageUrl440x240().isBlank()) {
                entities.add(GroupImage.create(group, req.imageUrl440x240(), sortOrder));
            }
            if (req.imageUrl100x100() != null && !req.imageUrl100x100().isBlank()) {
                entities.add(GroupImage.create(group, req.imageUrl100x100(), sortOrder));
            }
        }

        if (!entities.isEmpty()) {
            groupImageRepository.saveAll(entities);
        }

        Map<Integer, List<GroupImage>> byOrder = entities.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(
                        GroupImage::getSortOrder,
                        TreeMap::new,
                        Collectors.toList()
                ));

        return byOrder.values().stream()
                .map(this::toImageItem)
                .filter(Objects::nonNull)
                .toList();
    }

    private int sortOrderOrZero(Integer sortOrder) {
        return sortOrder != null ? sortOrder : 0;
    }

    /* =========================
     * Common Finders
     * ========================= */

    private Group findActiveGroup(Long groupId) {
        return groupRepository.findByIdAndDeletedAtIsNull(groupId)
                .orElseThrow(
                        () -> new GroupException(GroupErrorCode.GROUP_NOT_FOUND_BY_ID, groupId));
    }

    private User findMember(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new GroupException(GroupErrorCode.MEMBER_NOT_FOUND, userId));
    }

    /* =========================
     * Attend / Cancel Attend
     * ========================= */

    private void validateNotAlreadyAttend(GroupUser groupUser, Long groupId, Long userId) {
        if (groupUser != null && groupUser.getStatus() == ATTEND) {
            throw new GroupException(GroupErrorCode.ALREADY_ATTEND_GROUP, groupId, userId);
        }
    }

    private void validateCapacity(Group group) {
        long currentAttendCount = groupUserRepository.countByGroupAndStatus(group, ATTEND);
        if (currentAttendCount >= group.getMaxParticipants()) {
            throw new GroupException(GroupErrorCode.GROUP_CAPACITY_EXCEEDED, group.getId());
        }
    }

    private boolean upsertAttend(Group group, User member, GroupUser groupUser) {
        if (groupUser == null) {
            groupUserRepository.save(GroupUser.create(group, member, GroupRole.MEMBER));
            return true; // 최초 참여
        }
        groupUser.reAttend(); // LEFT -> ATTEND (재참여)
        return false;
    }

    @Transactional
    public GetGroupResponse attendGroup(CustomUserDetails userDetails, Long groupId) {
        Long userId = userDetails.getId();

        Group group = findActiveGroup(groupId);
        User member = findMember(userId);

        GroupUser groupUser = groupUserRepository.findByGroupAndUser(group, member).orElse(null);

        validateNotAlreadyAttend(groupUser, groupId, userId);
        validateCapacity(group);

        boolean firstJoin = upsertAttend(group, member, groupUser);
        if (firstJoin) {
            member.increaseGroupJoinedCount();
        }

        return buildGetGroupResponse(group, userId);
    }

    @Transactional
    public GetGroupResponse cancelAttendGroup(CustomUserDetails userDetails, Long groupId) {
        Long userId = userDetails.getId();

        Group group = findActiveGroup(groupId);
        User member = findMember(userId);

        GroupUser groupUser = findGroupUserOrThrow(group, member, groupId, userId);

        validateHostCannotLeave(groupUser, groupId, userId);
        validateCurrentlyAttend(groupUser, groupId, userId);

        groupUser.leave();

        return buildGetGroupResponse(group, userId);
    }

    private GroupUser findGroupUserOrThrow(Group group, User member, Long groupId, Long userId) {
        return groupUserRepository.findByGroupAndUser(group, member)
                .orElseThrow(
                        () -> new GroupException(GroupErrorCode.NOT_ATTEND_GROUP, groupId, userId));
    }

    private void validateHostCannotLeave(GroupUser groupUser, Long groupId, Long userId) {
        if (groupUser.getGroupRole() == GroupRole.HOST) {
            throw new GroupException(GroupErrorCode.HOST_CANNOT_LEAVE_OWN_GROUP, groupId, userId);
        }
    }

    private void validateCurrentlyAttend(GroupUser groupUser, Long groupId, Long userId) {
        if (groupUser.getStatus() == LEFT) {
            throw new GroupException(GroupErrorCode.NOT_ATTEND_GROUP, groupId, userId);
        }
    }

    /* =========================
     * Group List
     * ========================= */

    private int clampPageSize(int size) {
        return Math.max(1, Math.min(size, MAX_PAGE_SIZE));
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null) {
            return null;
        }
        String trimmed = keyword.trim();
        return trimmed.isBlank() ? null : trimmed;
    }

    @Transactional(readOnly = true)
    public GetGroupListResponse getGroupList(String keyword, Long cursor, int size) {
        int pageSize = clampPageSize(size);
        String normalizedKeyword = normalizeKeyword(keyword);

        List<Group> fetched = groupRepository.findGroupsWithKeywordAndCursor(
                normalizedKeyword, cursor, pageSize + 1
        );

        return toGroupListResponse(fetched, pageSize);
    }

    private GroupListItemResponse toGroupListItemResponse(Group group) {
        List<String> imageUrls = extractMainImageUrls(group);
        List<String> tagNames = extractTagNames(group);
        int participantCount = countAttenders(group);
        CreatedByResponse createdBy = CreatedByResponse.from(group.getHost());

        return GroupListItemResponse.of(group, imageUrls, tagNames, participantCount, createdBy);
    }

    /* =========================
     * Get Group (Detail)
     * ========================= */

    private GetGroupResponse buildGetGroupResponse(Group group, Long currentUserId) {
        List<GroupImageItemResponse> images = extractImageItems(group);
        List<String> tagNames = extractTagNames(group);

        List<GroupUser> attendingUsers = extractAttendingUsers(group);
        int participantCount = attendingUsers.size();

        List<GroupUser> attendingSorted = attendingUsers.stream()
                .sorted(Comparator.comparing(GroupUser::getJoinedAt))
                .toList();

        CreatedByResponse createdBy = CreatedByResponse.from(group.getHost());
        UserStatusResponse userStatus = resolveUserStatus(attendingUsers, currentUserId);

        List<JoinedMemberResponse> joinedMembers = attendingSorted.stream()
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

    private GetGroupResponse getGroupInternal(Long groupId, Long currentUserId) {
        Group group = findActiveGroup(groupId);
        return buildGetGroupResponse(group, currentUserId);
    }

    @Transactional(readOnly = true)
    public GetGroupResponse getGroup(CustomUserDetails userDetails, Long groupId) {
        return getGroupInternal(groupId, userDetails.getId());
    }

    @Transactional(readOnly = true)
    public GetGroupResponse getGroup(Long groupId) {
        return getGroupInternal(groupId, null);
    }

    /* =========================
     * Image / Tag / User extractors
     * ========================= */

    private Map<Integer, List<GroupImage>> groupImagesBySortOrder(Group group) {
        if (group.getImages() == null || group.getImages().isEmpty()) {
            return Map.of();
        }

        return group.getImages().stream()
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(
                        GroupImage::getSortOrder,
                        TreeMap::new,
                        Collectors.toList()
                ));
    }

    private List<GroupImageItemResponse> extractImageItems(Group group) {
        Map<Integer, List<GroupImage>> byOrder = groupImagesBySortOrder(group);
        if (byOrder.isEmpty()) {
            return List.of();
        }

        return byOrder.values().stream()
                .map(this::toImageItem)
                .filter(Objects::nonNull)
                .toList();
    }

    private List<String> extractMainImageUrls(Group group) {
        Map<Integer, List<GroupImage>> byOrder = groupImagesBySortOrder(group);
        if (byOrder.isEmpty()) {
            return List.of();
        }

        return byOrder.values().stream()
                .limit(GroupService.GROUP_LIST_IMAGE_LIMIT)
                .map(this::pickMainImageUrl)
                .filter(Objects::nonNull)
                .toList();
    }

    private GroupImageItemResponse toImageItem(List<GroupImage> images) {
        if (images == null || images.isEmpty()) {
            return null;
        }

        GroupImage main = findByTokenOrNull(images, MAIN_IMAGE_SIZE_TOKEN);
        if (main == null) {
            main = findFirstWithUrl(images);
        }
        if (main == null) {
            return null;
        }

        GroupImage thumb = findByTokenOrNull(images, THUMB_IMAGE_SIZE_TOKEN);
        return GroupImageItemResponse.from(main, thumb);
    }

    private String pickMainImageUrl(List<GroupImage> images) {
        if (images == null || images.isEmpty()) {
            return null;
        }

        GroupImage main = findByTokenOrNull(images, MAIN_IMAGE_SIZE_TOKEN);
        if (main != null) {
            return main.getImageUrl();
        }

        GroupImage any = findFirstWithUrl(images);
        return any != null ? any.getImageUrl() : null;
    }

    private GroupImage findFirstWithUrl(List<GroupImage> images) {
        return images.stream()
                .filter(Objects::nonNull)
                .filter(img -> img.getImageUrl() != null && !img.getImageUrl().isBlank())
                .findFirst()
                .orElse(null);
    }

    private GroupImage findByTokenOrNull(List<GroupImage> images, String token) {
        return images.stream()
                .filter(Objects::nonNull)
                .filter(img -> img.getImageUrl() != null && img.getImageUrl().contains(token))
                .findFirst()
                .orElse(null);
    }

    private List<String> extractTagNames(Group group) {
        if (group.getGroupTags() == null || group.getGroupTags().isEmpty()) {
            return List.of();
        }

        return group.getGroupTags().stream()
                .map(GroupTag::getTag)
                .filter(Objects::nonNull)
                .map(Tag::getName)
                .filter(Objects::nonNull)
                .toList();
    }

    private int countAttenders(Group group) {
        if (group.getUsers() == null || group.getUsers().isEmpty()) {
            return 0;
        }

        return (int) group.getUsers().stream()
                .filter(Objects::nonNull)
                .filter(gu -> gu.getStatus() == ATTEND)
                .count();
    }

    private List<GroupUser> extractAttendingUsers(Group group) {
        if (group.getUsers() == null || group.getUsers().isEmpty()) {
            return List.of();
        }

        return group.getUsers().stream()
                .filter(Objects::nonNull)
                .filter(gu -> gu.getStatus() == ATTEND)
                .toList();
    }

    private UserStatusResponse resolveUserStatus(List<GroupUser> attendingUsers,
            Long currentUserId) {
        if (currentUserId == null) {
            return UserStatusResponse.notJoined();
        }

        return attendingUsers.stream()
                .filter(gu -> gu.getUser() != null && currentUserId.equals(gu.getUser().getId()))
                .findFirst()
                .map(gu -> UserStatusResponse.fromJoined(gu.getJoinedAt()))
                .orElse(UserStatusResponse.notJoined());
    }

    private List<String> extractAllImageUrls(Group group) {
        if (group.getImages() == null || group.getImages().isEmpty()) {
            return List.of();
        }

        return group.getImages().stream()
                .filter(Objects::nonNull)
                .map(GroupImage::getImageUrl)
                .filter(url -> url != null && !url.isBlank())
                .toList();
    }

    /* =========================
     * Update / Delete
     * ========================= */

    private void updateGroupTags(Group group, List<String> tagNames) {
        if (tagNames == null) { // null이면 "태그는 건드리지 않는다"
            return;
        }
        group.getGroupTags().clear();
        saveGroupTags(group, tagNames);
    }

    private void validateUpdateGroupRequest(UpdateGroupRequest request, int currentAttendCount) {
        if (request.startTime() != null
                && request.endTime() != null
                && !request.endTime().isAfter(request.startTime())) {
            throw new GroupException(GroupErrorCode.INVALID_TIME_RANGE);
        }

        if (request.maxParticipants() == null || request.maxParticipants() < 2) {
            throw new GroupException(GroupErrorCode.INVALID_MAX_PARTICIPANTS);
        }

        if (request.maxParticipants() < currentAttendCount) {
            throw new GroupException(
                    GroupErrorCode.INVALID_MAX_PARTICIPANTS_LESS_THAN_CURRENT,
                    currentAttendCount
            );
        }
    }

    @Transactional
    public GetGroupResponse updateGroup(CustomUserDetails userDetails, Long groupId,
            UpdateGroupRequest request) {
        Long userId = userDetails.getId();
        Group group = findActiveGroup(groupId);

        if (!group.getHost().getId().equals(userId)) {
            throw new GroupException(GroupErrorCode.NO_PERMISSION_TO_UPDATE_GROUP, groupId, userId);
        }

        int currentAttendCount = countAttenders(group);
        validateUpdateGroupRequest(request, currentAttendCount);

        group.update(
                request.title(),
                request.location(),
                request.locationDetail(),
                request.startTime(),
                request.endTime(),
                request.description(),
                request.maxParticipants()
        );

        updateGroupTags(group, request.tags());

        return buildGetGroupResponse(group, userId);
    }

    @Transactional
    public void deleteGroup(CustomUserDetails userDetails, Long groupId) {
        Long userId = userDetails.getId();
        Group group = findActiveGroup(groupId);

        if (!group.getHost().getId().equals(userId)) {
            throw new GroupException(GroupErrorCode.NO_PERMISSION_TO_DELETE_GROUP, groupId, userId);
        }

        imageUploadService.deleteAllByUrls(extractAllImageUrls(group));
        groupRepository.delete(group);
    }

    @Transactional(readOnly = true)
    public GetGroupListResponse getMyGroups(CustomUserDetails userDetails, String type, Long cursor,
            int size) {
        MyGroupType myGroupType = MyGroupType.from(type);
        int pageSize = clampPageSize(size);

        return switch (myGroupType) {
            case CURRENT -> getMyCurrentGroups(userDetails.getId(), cursor, pageSize);
            case MY_POST -> getMyPostGroups(userDetails.getId(), cursor, pageSize);
            case PAST -> getMyPastGroups(userDetails.getId(), cursor, pageSize);
        };
    }

    private GetGroupListResponse getMyCurrentGroups(Long userId, Long cursor, int size) {
        LocalDateTime now = LocalDateTime.now();
        List<String> statuses = List.of(ATTEND.name());

        List<Group> fetched = groupRepository.findCurrentGroupsByUser(
                userId, statuses, cursor, now, size + 1
        );

        return toGroupListResponse(fetched, size);
    }

    private GetGroupListResponse getMyPostGroups(Long userId, Long cursor, int size) {
        List<Group> fetched = groupRepository.findMyPostGroupsByHost(
                userId, cursor, size + 1
        );

        return toGroupListResponse(fetched, size);
    }


    private GetGroupListResponse getMyPastGroups(Long userId, Long cursor, int size) {
        LocalDateTime now = LocalDateTime.now();
        List<String> statuses = List.of(ATTEND.name());

        List<Group> fetched = groupRepository.findPastGroupsByUser(
                userId, statuses, cursor, now, size + 1
        );

        return toGroupListResponse(fetched, size);
    }

    private GetGroupListResponse toGroupListResponse(List<Group> fetched, int pageSize) {
        boolean hasNext = fetched.size() > pageSize;

        List<Group> content = hasNext ? fetched.subList(0, pageSize) : fetched;
        Long nextCursor = hasNext ? content.get(pageSize - 1).getId() : null;

        List<GroupListItemResponse> items = content.stream()
                .map(this::toGroupListItemResponse)
                .toList();

        return GetGroupListResponse.of(items, nextCursor);
    }
}
