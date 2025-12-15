package team.wego.wegobackend.notification.application.dto.response;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import team.wego.wegobackend.notification.application.dto.NotificationType;
import team.wego.wegobackend.notification.domain.Notification;

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
    private Boolean isRead;
    private Long relatedId;
    private String relatedType;
    private String redirectUrl;
    private LocalDateTime createdAt;

    public static NotificationResponse from(Notification notification) {
        return NotificationResponse.builder()
            .id(notification.getId())
            .receiverId(notification.getReceiver().getId())
            .actorId(notification.getActor() != null ? notification.getActor().getId() : null)
            .actorNickname(notification.getActor() != null ? notification.getActor().getNickName() : null)
            .actorProfileImage(notification.getActor() != null ? notification.getActor().getProfileImage() : null)
            .type(notification.getType())
            .message(notification.getMessage())
            .isRead(notification.getIsRead())
            .relatedId(notification.getRelatedId())
            .relatedType(notification.getRelatedType())
            .redirectUrl(notification.getRedirectUrl())
            .createdAt(notification.getCreatedAt())
            .build();
    }
}
