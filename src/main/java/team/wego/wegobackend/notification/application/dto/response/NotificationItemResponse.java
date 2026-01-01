package team.wego.wegobackend.notification.application.dto.response;

import com.querydsl.core.annotations.QueryProjection;
import java.time.LocalDateTime;
import lombok.Getter;
import team.wego.wegobackend.group.v2.domain.entity.GroupV2;
import team.wego.wegobackend.notification.application.dto.NotificationType;
import team.wego.wegobackend.notification.domain.Notification;
import team.wego.wegobackend.user.domain.User;


@Getter
public class NotificationItemResponse {

    private final Long id;

    // 프론트: user { id, nickname }
    private final UserSummary user;

    // 프론트: group { id, title } | null
    private final GroupSummary group;

    // 프론트: 'follow' | 'group-join' ...
    private final String type;

    private final LocalDateTime createdAt;
    private final LocalDateTime readAt;
    private final String message;

    @Getter
    public static class UserSummary {

        private final Long id;
        private final String nickname;

        public UserSummary(Long id, String nickname) {
            this.id = id;
            this.nickname = nickname;
        }
    }

    @Getter
    public static class GroupSummary {

        private final Long id;
        private final String title;

        public GroupSummary(Long id, String title) {
            this.id = id;
            this.title = title;
        }
    }

    /**
     * QueryDSL projection constructor (flat fields)
     */
    @QueryProjection
    public NotificationItemResponse(
            Long id,
            Long actorId,
            String actorNickname,
            Long groupId,
            String groupTitle,
            NotificationType type,
            String message,
            LocalDateTime readAt,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.user = (actorId == null ? null : new UserSummary(actorId, actorNickname));
        this.group = (groupId == null ? null : new GroupSummary(groupId, groupTitle));
        this.type = NotificationTypeMapper.toClientType(type); // 아래 Mapper 사용
        this.message = message;
        this.readAt = readAt;
        this.createdAt = createdAt;
    }

    public static NotificationItemResponse of(Notification n, User actor, GroupV2 group) {
        return new NotificationItemResponse(
                n.getId(),
                actor == null ? null : actor.getId(),
                actor == null ? null : actor.getNickName(),
                group == null ? null : group.getId(),
                group == null ? null : group.getTitle(),
                n.getType(),
                n.getMessage(),
                n.getReadAt(),
                n.getCreatedAt()
        );
    }

    public static NotificationItemResponse of(
            Notification n,
            User actor,
            Long groupId,
            String groupTitle
    ) {
        return new NotificationItemResponse(
                n.getId(),
                actor == null ? null : actor.getId(),
                actor == null ? null : actor.getNickName(),
                groupId,
                groupTitle,
                n.getType(),
                n.getMessage(),
                n.getReadAt(),
                n.getCreatedAt()
        );
    }
}