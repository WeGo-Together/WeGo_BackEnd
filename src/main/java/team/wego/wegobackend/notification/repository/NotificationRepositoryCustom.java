package team.wego.wegobackend.notification.repository;

import java.util.List;
import team.wego.wegobackend.notification.application.dto.response.NotificationResponse;

public interface NotificationRepositoryCustom {

    List<NotificationResponse> findNotificationList(
        Long userId,
        Long cursorNotificationId,
        int size
    );
}
