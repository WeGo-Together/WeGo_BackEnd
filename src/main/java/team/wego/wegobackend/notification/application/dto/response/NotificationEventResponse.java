package team.wego.wegobackend.notification.application.dto.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import team.wego.wegobackend.group.v2.domain.entity.GroupV2;
import team.wego.wegobackend.notification.application.dto.NotificationType;
import team.wego.wegobackend.notification.domain.Notification;
import team.wego.wegobackend.user.domain.User;

@Getter
@Builder
public class NotificationEventResponse {

    private Long id;
    private String message;
    private NotificationType type;

    private ActorUserDto user; // 모임 생성자
    private GroupDto group;    // 모임

    public static NotificationEventResponse of(Notification n, User actor, GroupV2 group) {
        return NotificationEventResponse.builder()
                .id(n.getId())
                .message(n.getMessage())
                .type(n.getType())
                .user(ActorUserDto.from(actor))
                .group(GroupDto.from(group))
                .build();
    }


    @Getter(AccessLevel.PUBLIC)
    @Builder(access = AccessLevel.PUBLIC)
    public static class ActorUserDto {
        private Long id;
        private String nickName;
        private String profileImage;

        public static ActorUserDto from(User u) {
            return ActorUserDto.builder()
                    .id(u.getId())
                    .nickName(u.getNickName())
                    .profileImage(u.getProfileImage())
                    .build();
        }
    }

    @Getter(AccessLevel.PUBLIC)
    @Builder(access = AccessLevel.PUBLIC)
    public static class GroupDto {
        private Long id;
        private String title;

        public static GroupDto from(GroupV2 groupV2) {
            return GroupDto.builder()
                    .id(groupV2.getId())
                    .title(groupV2.getTitle())
                    .build();
        }
    }
}
