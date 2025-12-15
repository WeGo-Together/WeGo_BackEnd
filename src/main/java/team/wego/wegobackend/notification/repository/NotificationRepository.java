package team.wego.wegobackend.notification.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import team.wego.wegobackend.notification.domain.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

}
