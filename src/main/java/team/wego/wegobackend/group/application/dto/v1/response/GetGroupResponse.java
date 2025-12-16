package team.wego.wegobackend.group.application.dto.v1.response;

import java.time.LocalDateTime;
import java.util.List;
import team.wego.wegobackend.group.domain.entity.Group;
import team.wego.wegobackend.group.domain.entity.GroupRole;
import team.wego.wegobackend.group.domain.entity.GroupUser;
import team.wego.wegobackend.user.domain.User;

public record GetGroupResponse(
        Long id,
        String title,
        String location,
        String locationDetail,
        LocalDateTime startTime,
        LocalDateTime endTime,
        List<GroupImageItemResponse> images,
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

    public static GetGroupResponse of(
            Group group,
            List<GroupImageItemResponse> images,
            List<String> tagNames,
            int participantCount,
            CreatedByResponse createdBy,
            UserStatusResponse userStatus,
            List<JoinedMemberResponse> joinedMembers
    ) {
        return new GetGroupResponse(
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

    // createdBy
    public record CreatedByResponse(
            Long userId,
            String nickName,
            String profileImage,
            String profileMessage
    ) {

        public static CreatedByResponse from(User host) {
            return new CreatedByResponse(
                    host.getId(),
                    host.getNickName(),
                    host.getProfileImage(),
                    host.getProfileMessage()
            );
        }
    }

    // userStatus
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

    // joinedMembers
    public record JoinedMemberResponse(
            Long userId,
            GroupRole groupRole,
            String nickName,
            String profileImage,
            LocalDateTime joinedAt
    ) {

        public static JoinedMemberResponse from(
                GroupUser groupUser
        ) {
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
}


