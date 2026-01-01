package team.wego.wegobackend.notification.repository;

import java.util.List;
import team.wego.wegobackend.notification.application.dto.response.NotificationItemResponse;
import team.wego.wegobackend.notification.application.dto.response.NotificationResponse;

public interface NotificationRepositoryCustom {

    List<NotificationItemResponse> findNotificationList(
            Long userId,
            Long cursorNotificationId,
            int size
    );

}
