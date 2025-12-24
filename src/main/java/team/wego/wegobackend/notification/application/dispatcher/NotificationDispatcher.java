package team.wego.wegobackend.notification.application.dispatcher;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import team.wego.wegobackend.group.v2.application.event.NotificationEvent;
import team.wego.wegobackend.group.v2.domain.entity.GroupV2;
import team.wego.wegobackend.notification.application.SseEmitterService;
import team.wego.wegobackend.notification.domain.Notification;
import team.wego.wegobackend.notification.repository.NotificationRepository;
import team.wego.wegobackend.user.domain.User;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationDispatcher {

    private final NotificationRepository notificationRepository;
    private final SseEmitterService sseEmitterService;

    // 공통 알림 전송 파이프라인
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void dispatch(
            List<Notification> notifications,
            User actor,
            GroupV2 group
    ) {
        if (notifications == null || notifications.isEmpty()) {
            return;
        }


        notificationRepository.saveAll(notifications); // 저장
        notificationRepository.flush(); // ID 보장
        log.info("[NOTI] saved notifications size={}", notifications.size());


        // SSE 전송
        for (Notification notification : notifications) {
            NotificationEvent event =
                    NotificationEvent.of(notification, actor, group);

            Long receiverId = notification.getReceiver().getId();
            sseEmitterService.sendNotification(receiverId, event);
        }
    }
}

