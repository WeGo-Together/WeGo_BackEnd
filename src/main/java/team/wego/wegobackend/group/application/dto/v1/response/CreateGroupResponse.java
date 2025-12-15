package team.wego.wegobackend.group.application.dto.v1.response;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import team.wego.wegobackend.group.domain.entity.Group;
import team.wego.wegobackend.group.domain.entity.GroupTag;
import team.wego.wegobackend.group.domain.entity.GroupUserStatus;
import team.wego.wegobackend.tag.domain.entity.Tag;
import team.wego.wegobackend.user.domain.User;

@Builder(access = AccessLevel.PRIVATE)
public record CreateGroupResponse(
        Long id,
        String title,
        String location,
        String locationDetail,
        LocalDateTime startTime,
        LocalDateTime endTime,
        List<String> tags,
        String description,
        long participantCount,
        int maxParticipants,
        CreatedBy createdBy,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<GroupImageItemResponse> images
) {

    public static CreateGroupResponse from(Group group) {
        return from(group, List.of());
    }

    public static CreateGroupResponse from(
            Group group,
            List<GroupImageItemResponse> images
    ) {
        // 태그 이름 리스트
        List<String> tagNames = group.getGroupTags().stream()
                .map(GroupTag::getTag)
                .map(Tag::getName)
                .toList();

        // 현재 참여 인원
        long attendUserCount = group.getUsers().stream()
                .filter(groupUser -> GroupUserStatus.ATTEND.equals(groupUser.getStatus()))
                .count();

        // 모임 생성자 정보
        User host = group.getHost();

        CreatedBy createdByHost = new CreatedBy(
                host.getId(),
                host.getNickName(),
                host.getProfileImage()
        );

        return CreateGroupResponse.builder()
                .id(group.getId())
                .title(group.getTitle())
                .location(group.getLocation())
                .locationDetail(group.getLocationDetail())
                .startTime(group.getStartTime())
                .endTime(group.getEndTime())
                .tags(tagNames)
                .description(group.getDescription())
                .participantCount(attendUserCount)
                .maxParticipants(group.getMaxParticipants())
                .createdBy(createdByHost)
                .createdAt(group.getCreatedAt())
                .updatedAt(group.getUpdatedAt())
                .images(images)
                .build();
    }
}
