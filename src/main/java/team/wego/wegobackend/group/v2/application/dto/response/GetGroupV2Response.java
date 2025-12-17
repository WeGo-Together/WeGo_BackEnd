package team.wego.wegobackend.group.v2.application.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import team.wego.wegobackend.group.domain.entity.Group;
import team.wego.wegobackend.group.domain.entity.GroupImage;
import team.wego.wegobackend.group.domain.entity.GroupRole;
import team.wego.wegobackend.group.domain.entity.GroupUser;
import team.wego.wegobackend.user.domain.User;


public record GetGroupV2Response(
        Long id,
        String title,
        String location,
        String locationDetail,
        LocalDateTime startTime,
        LocalDateTime endTime,
        List<GroupImageV2ItemResponse> images,
        List<String> tags,
        String description,
        int participantCount,
        int maxParticipants,
        CreatedByResponse createdBy,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        UserStatusResponse userStatus,
        List<JoinedMemberResponse> joinedMembers
) {

    public static GetGroupV2Response of(
            Group group,
            List<GroupImageV2ItemResponse> images,
            List<String> tagNames,
            int participantCount,
            CreatedByResponse createdBy,
            UserStatusResponse userStatus,
            List<JoinedMemberResponse> joinedMembers
    ) {
        return new GetGroupV2Response(
                group.getId(),
                group.getTitle(),
                group.getLocation(),
                group.getLocationDetail(),
                group.getStartTime(),
                group.getEndTime(),
                images,
                tagNames,
                group.getDescription(),
                participantCount,
                group.getMaxParticipants(),
                createdBy,
                group.getCreatedAt(),
                group.getUpdatedAt(),
                userStatus,
                joinedMembers
        );
    }

    public record CreatedByResponse(
            Long userId,
            String nickName,
            String profileImage
    ) {

        public static CreatedByResponse from(User host) {
            return new CreatedByResponse(host.getId(), host.getNickName(), host.getProfileImage());
        }

    }

    public record UserStatusResponse(
            boolean isJoined,
            LocalDateTime joinedAt
    ) {

        public static UserStatusResponse notJoined() {
            return new UserStatusResponse(false, null);
        }

        public static UserStatusResponse fromJoined(LocalDateTime joinedAt) {
            return new UserStatusResponse(true, joinedAt);
        }
    }

    public record JoinedMemberResponse(
            Long userId,
            GroupRole groupRole,
            String nickName,
            String profileImage,
            LocalDateTime joinedAt
    ) {

        public static JoinedMemberResponse from(GroupUser groupUser) {
            User user = groupUser.getUser();
            return new JoinedMemberResponse(
                    user.getId(),
                    groupUser.getGroupRole(),
                    user.getNickName(),
                    user.getProfileImage(),
                    groupUser.getJoinedAt()
            );
        }
    }

    public record GroupImageV2ItemResponse(
            int sortOrder,
            Long imageId440x240,
            Long imageId100x100,
            String imageUrl440x240,
            String imageUrl100x100
    ) {

        public static GroupImageV2ItemResponse from(
                GroupImage main,   // 440x240
                GroupImage thumb   // 100x100: nullable
        ) {
            Long mainId = (main != null) ? main.getId() : null;
            Long thumbId = (thumb != null) ? thumb.getId() : null;

            int sortOrder = (main != null)
                    ? main.getSortOrder()
                    : (thumb != null ? thumb.getSortOrder() : 0);

            String mainUrlVal = (main != null) ? main.getImageUrl() : null;
            String thumbUrlVal = (thumb != null) ? thumb.getImageUrl() : null;

            return new GroupImageV2ItemResponse(
                    sortOrder,
                    mainId,
                    thumbId,
                    mainUrlVal,
                    thumbUrlVal
            );
        }

        public static GroupImageV2ItemResponse fromMainOnly(GroupImage main) {
            return from(main, null);
        }
    }
}