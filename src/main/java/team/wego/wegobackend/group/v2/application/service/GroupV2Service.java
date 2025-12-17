package team.wego.wegobackend.group.v2.application.service;

import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.wego.wegobackend.common.security.CustomUserDetails;
import team.wego.wegobackend.group.domain.exception.GroupErrorCode;
import team.wego.wegobackend.group.domain.exception.GroupException;
import team.wego.wegobackend.group.v2.application.dto.request.CreateGroupImageV2Request;
import team.wego.wegobackend.group.v2.application.dto.request.CreateGroupV2Request;
import team.wego.wegobackend.group.v2.application.dto.response.CreateGroupV2Response;
import team.wego.wegobackend.group.v2.application.dto.response.GetGroupListV2Response;
import team.wego.wegobackend.group.v2.application.dto.response.GetGroupListV2Response.GroupListItemV2Response;
import team.wego.wegobackend.group.v2.application.dto.response.GetGroupListV2Response.GroupListItemV2Response.CreatedByV2Response;
import team.wego.wegobackend.group.v2.domain.entity.GroupImageV2;
import team.wego.wegobackend.group.v2.domain.entity.GroupTagV2;
import team.wego.wegobackend.group.v2.domain.entity.GroupUserV2;
import team.wego.wegobackend.group.v2.domain.entity.GroupUserV2Role;
import team.wego.wegobackend.group.v2.domain.entity.GroupV2;
import team.wego.wegobackend.group.v2.domain.entity.GroupV2Address;
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
    public CreateGroupV2Response create(CustomUserDetails userDetails,
            CreateGroupV2Request request) {

        // 회원 조회
        User host = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new GroupException(GroupErrorCode.HOST_USER_NOT_FOUND,
                        userDetails.getId())
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
}
