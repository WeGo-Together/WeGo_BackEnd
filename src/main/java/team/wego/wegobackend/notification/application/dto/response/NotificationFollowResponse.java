package team.wego.wegobackend.notification.application.dto.response;

import java.time.LocalDateTime;
import lombok.Getter;
import team.wego.wegobackend.notification.domain.Notification;
import team.wego.wegobackend.user.domain.User;

@Getter
public class NotificationFollowResponse {

    private final Long id;

    private final UserSummary user;

    private final String type;

    private final LocalDateTime createdAt;
    private final LocalDateTime readAt;
    private final String message;

    public NotificationFollowResponse(Notification n, User actor) {
        this.id = n.getId();
        this.user = new UserSummary(actor.getId(), actor.getNickName());
        this.type = NotificationTypeMapper.toClientType(n.getType());
        this.createdAt = n.getCreatedAt();
        this.readAt = n.getReadAt();
        this.message = n.getMessage();
    }

}
