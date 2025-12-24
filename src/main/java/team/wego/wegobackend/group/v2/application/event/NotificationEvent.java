package team.wego.wegobackend.group.v2.application.event;

import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import team.wego.wegobackend.group.v2.domain.entity.GroupV2;
import team.wego.wegobackend.notification.application.dto.NotificationType;
import team.wego.wegobackend.notification.domain.Notification;
import team.wego.wegobackend.user.domain.User;

@Getter(AccessLevel.PUBLIC)
@Builder(access = AccessLevel.PUBLIC)
public class NotificationEvent {

    private Long id; // notificationId
    private String message;
    private NotificationType type;

    private LocalDateTime createdAt;
    private LocalDateTime readAt; // null == unread

    private ActorUser user; // 모임 생성자
    private GroupInfo group; // 모임

    public boolean isRead() {
        return readAt != null;
    }

    public static NotificationEvent of(Notification n, User actor, GroupV2 group) {
        return NotificationEvent.builder()
                .id(n.getId())
                .message(n.getMessage())
                .type(n.getType())
                .createdAt(n.getCreatedAt())
                .readAt(n.getReadAt())
                .user(ActorUser.from(actor))
                .group(group != null ? GroupInfo.from(group) : null)
                .build();
    }

    @Getter(AccessLevel.PUBLIC)
    @Builder(access = AccessLevel.PUBLIC)
    public static class ActorUser {
        private Long id;
        private String nickName;
        private String profileImage;

        public static ActorUser from(User u) {
            if (u == null) return null;
            return ActorUser.builder()
                    .id(u.getId())
                    .nickName(u.getNickName())
                    .profileImage(u.getProfileImage())
                    .build();
        }
    }

    @Getter(AccessLevel.PUBLIC)
    @Builder(access = AccessLevel.PUBLIC)
    public static class GroupInfo {
        private Long id;
        private String title;

        public static GroupInfo from(GroupV2 g) {
            if (g == null) return null;
            return GroupInfo.builder()
                    .id(g.getId())
                    .title(g.getTitle())
                    .build();
        }
    }
}

