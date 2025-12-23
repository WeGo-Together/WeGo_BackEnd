package team.wego.wegobackend.notification.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import team.wego.wegobackend.notification.domain.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Long>, NotificationRepositoryCustom {

    @Query("""
    select count(n)
    from Notification n
    where n.receiver.id = :userId
      and n.readAt is null
""")
    long countUnread(@Param("userId") Long userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
    update Notification n
    set n.readAt = CURRENT_TIMESTAMP
    where n.receiver.id = :userId
      and n.readAt is null
""")
    int markAllAsRead(@Param("userId") Long userId);

}
