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

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void dispatch(
            List<Notification> notifications,
            User actor,
            GroupV2 group
    ) {

        if (notifications == null || notifications.isEmpty()) {
            return;
        }



        // 저장 결과를 받아서 "ID 확정된 엔티티"로 SSE 전송
        List<Notification> saved = notificationRepository.saveAll(notifications);
        notificationRepository.flush();
        log.info("[NOTI][DISPATCH] saved={} actorId={} groupId={}",
                saved.size(), (actor == null ? null : actor.getId()),
                (group == null ? null : group.getId()));

        int sent = 0;
        int noEmitter = 0;


        for (Notification n : saved) {
            Long receiverId = n.getReceiver().getId();
            boolean ok = sseEmitterService.sendNotificationIfConnected(
                    receiverId, NotificationEvent.of(n, actor, group)
            );
            if (ok) sent++; else noEmitter++;
        }
        log.info("[NOTI][DISPATCH] sseSent={} noEmitter={}", sent, noEmitter);
    }
}

