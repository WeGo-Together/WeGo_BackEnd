package team.wego.wegobackend.notification.application.dto.response;

import java.util.List;

public record NotificationListResponse(List<NotificationResponse> notifications, Long nextCursor) {

}
