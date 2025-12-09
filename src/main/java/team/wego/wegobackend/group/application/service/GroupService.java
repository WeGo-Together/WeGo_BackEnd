package team.wego.wegobackend.group.application.service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.wego.wegobackend.group.application.dto.request.CreateGroupRequest;
import team.wego.wegobackend.group.application.dto.response.CreateGroupResponse;
import team.wego.wegobackend.group.domain.entity.Group;
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

    private final GroupImageService groupImageService;

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
    public CreateGroupResponse create(Long userId, CreateGroupRequest request) {
        // 1. 회원 조회(HOST) TODO: 회원 정보 파싱 구현되면 연결
        User host = userRepository.findById(userId)
                .orElseThrow(() -> new GroupException(GroupErrorCode.HOST_USER_NOT_FOUND, userId));

        // 2. 비즈니스 유효성 검사
        validateCreateGroupRequest(request);

        // 3. Group 엔티티 생성 및 저장
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

        // 4. 태그 저장
        saveGroupTags(group, request.tags());

        // 5. 모임 생성자를 모임 참가자(HOST)로 등록
        saveHostAsGroupUser(group, host);

        return CreateGroupResponse.from(group);
    }
}
