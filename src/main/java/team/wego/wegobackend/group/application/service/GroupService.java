package team.wego.wegobackend.group.application.service;

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
import team.wego.wegobackend.group.application.dto.response.CreateGroupResponse;
import team.wego.wegobackend.group.application.dto.response.GroupImageItemResponse;
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

    private void validateCreateGroupRequest(CreateGroupRequest request) {
        if (!request.endTime().isAfter(request.startTime())) {
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

        // 정렬 보장(필요 시 sortOrder 기준으로 정렬)
        List<CreateGroupImageRequest> sorted = imageRequests.stream()
                .filter(Objects::nonNull)
                .sorted((a, b) -> {
                    int s1 = a.sortOrder() != null ? a.sortOrder() : 0;
                    int s2 = b.sortOrder() != null ? b.sortOrder() : 0;
                    return Integer.compare(s1, s2);
                })
                .toList();

        // 엔티티 생성
        List<GroupImage> images = sorted.stream()
                .map(req -> {
                    int sortOrder = req.sortOrder() != null ? req.sortOrder() : 0;
                    // DB에는 대표 이미지(440x240)만 저장
                    return GroupImage.create(group, req.imageUrl440x240(), sortOrder);
                })
                .toList();

        // 저장
        if (!images.isEmpty()) {
            groupImageRepository.saveAll(images);
        }

        // 응답 DTO로 변환
        return images.stream()
                .map(image -> {
                    // 요청에서 매칭되는 썸네일 URL 찾아오기
                    CreateGroupImageRequest matched = sorted.stream()
                            .filter(req -> Objects.equals(
                                    req.imageUrl440x240(),
                                    image.getImageUrl()
                            ))
                            .findFirst()
                            .orElse(null);

                    String thumbUrl = matched != null ? matched.imageUrl100x100() : null;

                    return GroupImageItemResponse.from(
                            image,
                            image.getImageUrl(), // 440x240
                            thumbUrl             // 100x100
                    );
                })
                .toList();
    }
}
