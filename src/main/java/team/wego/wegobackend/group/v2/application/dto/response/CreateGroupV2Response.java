package team.wego.wegobackend.group.v2.application.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import team.wego.wegobackend.group.v2.application.dto.common.Address;
import team.wego.wegobackend.group.v2.application.dto.common.CreatedBy;
import team.wego.wegobackend.group.v2.application.dto.common.GroupImageItem;
import team.wego.wegobackend.group.v2.domain.entity.GroupTagV2;
import team.wego.wegobackend.group.v2.domain.entity.GroupUserV2;
import team.wego.wegobackend.group.v2.domain.entity.GroupUserV2Role;
import team.wego.wegobackend.group.v2.domain.entity.GroupUserV2Status;
import team.wego.wegobackend.group.v2.domain.entity.GroupV2;
import team.wego.wegobackend.group.v2.domain.entity.GroupV2JoinPolicy;
import team.wego.wegobackend.group.v2.domain.entity.GroupV2Status;
import team.wego.wegobackend.tag.domain.entity.Tag;
import team.wego.wegobackend.user.domain.User;

public record CreateGroupV2Response(
        Long id,
        String title,
        GroupV2JoinPolicy joinPolicy,
        GroupV2Status status,
        Address address,
        LocalDateTime startTime,
        LocalDateTime endTime,
        List<String> tags,
        String description,
        long participantCount,
        int maxParticipants,
        CreatedBy createdBy,
        Membership myMembership,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<GroupImageItem> images
) {

    public static CreateGroupV2Response from(GroupV2 group, User currentUser) {

        List<String> tagNames = group.getGroupTags().stream()
                .map(GroupTagV2::getTag)
                .map(Tag::getName)
                .toList();

        long attendCount = group.getUsers().stream()
                .filter(u -> u.getStatus() == GroupUserV2Status.ATTEND)
                .count();

        GroupUserV2 myGroupUser = group.getUsers().stream()
                .filter(groupUserV2 -> groupUserV2.getUser().getId().equals(currentUser.getId()))
                .findFirst()
                .orElse(null);

        return new CreateGroupV2Response(
                group.getId(),
                group.getTitle(),
                group.getJoinPolicy(),
                group.getStatus(),
                Address.from(group.getAddress()),
                group.getStartTime(),
                group.getEndTime(),
                tagNames,
                group.getDescription(),
                attendCount,
                group.getMaxParticipants(),
                CreatedBy.from(group.getHost()),
                myGroupUser == null ? null : Membership.from(myGroupUser),
                group.getCreatedAt(),
                group.getUpdatedAt(),
                group.getImages().stream().map(GroupImageItem::from).toList()
        );
    }

    public record Membership(
            Long groupUserId,
            Long userId,
            GroupUserV2Role role,
            GroupUserV2Status status,
            LocalDateTime joinedAt,
            LocalDateTime leftAt
    ) {

        public static Membership from(GroupUserV2 gu) {
            return new Membership(
                    gu.getId(),
                    gu.getUser().getId(),
                    gu.getGroupRole(),
                    gu.getStatus(),
                    gu.getJoinedAt(),
                    gu.getLeftAt()
            );
        }
    }
}
