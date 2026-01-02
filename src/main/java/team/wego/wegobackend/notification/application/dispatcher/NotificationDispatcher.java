package team.wego.wegobackend.notification.application.dispatcher;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import team.wego.wegobackend.group.v2.domain.entity.GroupV2;
import team.wego.wegobackend.notification.application.SseEmitterService;
import team.wego.wegobackend.notification.application.dto.response.NotificationFollowResponse;
import team.wego.wegobackend.notification.application.dto.response.NotificationItemResponse;
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

            NotificationItemResponse payload = NotificationItemResponse.of(n, actor, group);

            boolean ok = sseEmitterService.sendNotificationIfConnected(receiverId, payload);

            if (ok) {
                sent++;
            } else {
                noEmitter++;
            }
        }
        log.info("[NOTI][DISPATCH] sseSent={} noEmitter={}", sent, noEmitter);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void dispatch(
            List<Notification> notifications,
            User actor,
            Long groupId,
            String groupTitle
    ) {
        if (notifications == null || notifications.isEmpty()) return;

        List<Notification> saved = notificationRepository.saveAll(notifications);
        notificationRepository.flush();

        log.info("[NOTI][DISPATCH] saved={} actorId={} groupSnapshotId={}",
                saved.size(), (actor == null ? null : actor.getId()), groupId);

        int sent = 0;
        int noEmitter = 0;

        for (Notification n : saved) {
            Long receiverId = n.getReceiver().getId();

            // 모임 삭제 시 groupId, title 포함하기 위해 DTO에 직접 주입
            NotificationItemResponse payload =
                    NotificationItemResponse.of(n, actor, groupId, groupTitle);

            boolean ok = sseEmitterService.sendNotificationIfConnected(receiverId, payload);

            if (ok) sent++;
            else noEmitter++;
        }

        log.info("[NOTI][DISPATCH] sseSent={} noEmitter={}", sent, noEmitter);
    }

    @Transactional
    public void dispatch(
        User follower,
        User follow
    ) {

        Notification notification = Notification.createFollowNotification(follow, follower);

        notificationRepository.save(notification);


        sseEmitterService.sendNotificationIfConnected(follow.getId(), new NotificationFollowResponse(notification, follower));

    }
}

