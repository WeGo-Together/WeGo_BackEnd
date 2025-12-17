package team.wego.wegobackend.group.v2.application.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import team.wego.wegobackend.group.v2.application.dto.common.Address;
import team.wego.wegobackend.group.v2.application.dto.common.CreatedBy;
import team.wego.wegobackend.group.v2.application.dto.common.GroupImageItem;
import team.wego.wegobackend.group.v2.domain.entity.GroupImageV2;
import team.wego.wegobackend.group.v2.domain.entity.GroupTagV2;
import team.wego.wegobackend.group.v2.domain.entity.GroupUserV2;
import team.wego.wegobackend.group.v2.domain.entity.GroupUserV2Role;
import team.wego.wegobackend.group.v2.domain.entity.GroupUserV2Status;
import team.wego.wegobackend.group.v2.domain.entity.GroupV2;
import team.wego.wegobackend.group.v2.domain.entity.GroupV2Status;
import team.wego.wegobackend.tag.domain.entity.Tag;
import team.wego.wegobackend.user.domain.User;

public record GetGroupV2Response(
        Long id,
        String title,
        GroupV2Status status,
        Address address,
        LocalDateTime startTime,
        LocalDateTime endTime,
        List<GroupImageItem> images,
        List<String> tags,
        String description,
        long participantCount,
        int maxParticipants,
        CreatedBy createdBy,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        MyMembership myMembership,           // 로그인 아니면 null
        List<JoinedMember> joinedMembers     // 참여자 목록(ATTEND)
) {

    public static GetGroupV2Response of(
            GroupV2 group,
            List<GroupImageV2> images,
            List<GroupUserV2> users,
            Long userIdOrNull
    ) {
        List<String> tagNames = group.getGroupTags().stream()
                .map(GroupTagV2::getTag)
                .map(Tag::getName)
                .toList();

        long attendCount = users.stream()
                .filter(gu -> gu.getStatus() == GroupUserV2Status.ATTEND)
                .count();

        List<JoinedMember> joinedMembers = users.stream()
                .filter(gu -> gu.getStatus() == GroupUserV2Status.ATTEND)
                .map(JoinedMember::from)
                .toList();

        MyMembership myMembership = (userIdOrNull == null)
                ? null
                : MyMembership.from(users, userIdOrNull);

        List<GroupImageItem> imageItems = images.stream()
                .map(GroupImageItem::from)
                .toList();

        return new GetGroupV2Response(
                group.getId(),
                group.getTitle(),
                group.getStatus(),
                Address.from(group.getAddress()),
                group.getStartTime(),
                group.getEndTime(),
                imageItems,
                tagNames,
                group.getDescription(),
                attendCount,
                group.getMaxParticipants(),
                CreatedBy.from(group.getHost()),
                group.getCreatedAt(),
                group.getUpdatedAt(),
                myMembership,
                joinedMembers
        );
    }

    public record MyMembership(
            boolean isJoined,
            Long groupUserId,
            GroupUserV2Role role,
            GroupUserV2Status status,
            LocalDateTime joinedAt,
            LocalDateTime leftAt
    ) {
        public static MyMembership from(List<GroupUserV2> users, Long userId) {
            return users.stream()
                    .filter(gu -> gu.getUser().getId().equals(userId))
                    .findFirst()
                    .map(gu -> new MyMembership(
                            gu.getStatus() == GroupUserV2Status.ATTEND,
                            gu.getId(),
                            gu.getGroupRole(),
                            gu.getStatus(),
                            gu.getJoinedAt(),
                            gu.getLeftAt()
                    ))
                    .orElse(new MyMembership(false, null, null, null, null, null));
        }
    }

    public record JoinedMember(
            Long userId,
            Long groupUserId,
            GroupUserV2Role role,
            String nickName,
            String profileImage,
            LocalDateTime joinedAt
    ) {
        public static JoinedMember from(GroupUserV2 groupUser) {
            User user = groupUser.getUser();
            return new JoinedMember(
                    user.getId(),
                    groupUser.getId(),
                    groupUser.getGroupRole(),
                    user.getNickName(),
                    user.getProfileImage(),
                    groupUser.getJoinedAt()
            );
        }
    }
}

