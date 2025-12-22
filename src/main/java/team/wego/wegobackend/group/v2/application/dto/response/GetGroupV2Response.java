package team.wego.wegobackend.group.v2.application.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import team.wego.wegobackend.group.v2.application.dto.common.Address;
import team.wego.wegobackend.group.v2.application.dto.common.CreatedBy;
import team.wego.wegobackend.group.v2.application.dto.common.GroupImageItem;
import team.wego.wegobackend.group.v2.application.dto.common.MyMembership;
import team.wego.wegobackend.group.v2.domain.entity.GroupImageV2;
import team.wego.wegobackend.group.v2.domain.entity.GroupTagV2;
import team.wego.wegobackend.group.v2.domain.entity.GroupUserV2;
import team.wego.wegobackend.group.v2.domain.entity.GroupUserV2Role;
import team.wego.wegobackend.group.v2.domain.entity.GroupUserV2Status;
import team.wego.wegobackend.group.v2.domain.entity.GroupV2;
import team.wego.wegobackend.group.v2.domain.entity.GroupV2JoinPolicy;
import team.wego.wegobackend.group.v2.domain.entity.GroupV2Status;
import team.wego.wegobackend.tag.domain.entity.Tag;
import team.wego.wegobackend.user.domain.User;

public record GetGroupV2Response(
        Long id,
        String title,
        GroupV2JoinPolicy joinPolicy,
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
        MyMembership myMembership,            // 로그인 아니면 null
        List<JoinedMember> joinedMembers      // Host면 전체, 아니면 ATTEND만
) {

    public static GetGroupV2Response of(
            GroupV2 group,
            List<GroupImageV2> images,
            List<GroupUserV2> users,
            Long userIdOrNull
    ) {
        // 태그
        List<String> tagNames = group.getGroupTags().stream()
                .map(GroupTagV2::getTag)
                .map(Tag::getName)
                .toList();

        // 참가자 수: ATTEND만 세기
        long attendCount = users.stream()
                .filter(groupUserV2 -> groupUserV2.getStatus() == GroupUserV2Status.ATTEND)
                .count();

        boolean isHostViewer = Objects.equals(group.getHost().getId(), userIdOrNull);

        // 모임 참가자들
        // HOST: 모든 상태 확인 가능
        // 비회원, 사용자: ATTEND만 확인 가능
        List<JoinedMember> joinedMembers;

        if (isHostViewer) { // HOST: 전원 노출 + status/leftAt 노출
            joinedMembers = users.stream()
                    .map(groupUserV2 -> JoinedMember.from(groupUserV2, true))
                    .toList();
        } else { // 비HOST: ATTEND만 노출
            joinedMembers = users.stream()
                    .filter(groupUserV2 -> groupUserV2.getStatus() == GroupUserV2Status.ATTEND)
                    .map(groupUserV2 -> JoinedMember.from(groupUserV2, true)) // 여기서는 ATTEND만 들어오니 status 노출해도 OK
                    .toList();
        }

        // myMembership (로그인된 사용자만)
        MyMembership myMembership = (userIdOrNull == null)
                ? null
                : MyMembership.from(users, userIdOrNull);

        // images
        List<GroupImageItem> imageItems = images.stream()
                .map(GroupImageItem::from)
                .toList();

        return new GetGroupV2Response(
                group.getId(),
                group.getTitle(),
                group.getJoinPolicy(),
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

    public record JoinedMember(
            Long userId,
            Long groupUserId,
            GroupUserV2Role role,
            GroupUserV2Status status,
            String nickName,
            String profileImage,
            LocalDateTime joinedAt,
            LocalDateTime leftAt
    ) {

        public static JoinedMember from(GroupUserV2 groupUser, boolean exposeStatus) {
            User user = groupUser.getUser();

            return new JoinedMember(
                    user.getId(),
                    groupUser.getId(),
                    groupUser.getGroupRole(),
                    exposeStatus ? groupUser.getStatus() : null,
                    user.getNickName(),
                    user.getProfileImage(),
                    groupUser.getJoinedAt(),
                    exposeStatus ? groupUser.getLeftAt() : null
            );
        }
    }
}
