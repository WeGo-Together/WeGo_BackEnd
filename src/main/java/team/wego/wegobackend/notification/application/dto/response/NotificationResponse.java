package team.wego.wegobackend.notification.application.dto.response;

import com.querydsl.core.annotations.QueryProjection;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import team.wego.wegobackend.notification.application.dto.NotificationType;
import team.wego.wegobackend.notification.domain.Notification;
import team.wego.wegobackend.user.domain.User;

@Getter
@Builder
public class NotificationResponse {

    private Long id;
    private Long receiverId;
    private Long actorId;
    private String actorNickname;
    private String actorProfileImage;
    private NotificationType type;
    private String message;
    private LocalDateTime readAt;
    private Long relatedId;
    private String relatedType;
    private String redirectUrl;
    private LocalDateTime createdAt;

    public static NotificationResponse from(Notification notification) {
        return NotificationResponse.builder()
            .id(notification.getId())
            .receiverId(notification.getReceiver().getId())
            .actorId(notification.getActor() != null ? notification.getActor().getId() : null)
            .actorNickname(
                notification.getActor() != null ? notification.getActor().getNickName() : null)
            .actorProfileImage(
                notification.getActor() != null ? notification.getActor().getProfileImage() : null)
            .type(notification.getType())
            .message(notification.getMessage())
            .readAt(notification.getReadAt())
            .relatedId(notification.getRelatedId())
            .relatedType(notification.getRelatedType())
            .redirectUrl(notification.getRedirectUrl())
            .createdAt(notification.getCreatedAt())
            .build();
    }

    public static NotificationResponse from(User user) {
        return NotificationResponse.builder()
            .id(user.getId())
            .receiverId(user.getId())
            .actorId(user.getId())
            .actorNickname(user.getNickName())
            .actorProfileImage(user.getProfileImage())
            .type(NotificationType.TEST)
            .message("테스트 알림 응답")
            .relatedId(null)
            .relatedType("TEST")
            .redirectUrl("https://api.wego.monster/swagger-ui/index.html")
            .createdAt(LocalDateTime.now())
            .build();
    }

    @QueryProjection
    public NotificationResponse(
        Long id,
        Long receiverId,
        Long actorId,
        String actorNickname,
        String actorProfileImage,
        NotificationType type,
        String message,
        LocalDateTime readAt,
        Long relatedId,
        String relatedType,
        String redirectUrl,
        LocalDateTime createdAt
    ) {
        this.id = id;
        this.receiverId = receiverId;
        this.actorId = actorId;
        this.actorNickname = actorNickname;
        this.actorProfileImage = actorProfileImage;
        this.type = type;
        this.message = message;
        this.readAt = readAt;
        this.relatedId = relatedId;
        this.relatedType = relatedType;
        this.redirectUrl = redirectUrl;
        this.createdAt = createdAt;
    }
}
