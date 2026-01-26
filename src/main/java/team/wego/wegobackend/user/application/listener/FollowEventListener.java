package team.wego.wegobackend.user.application.listener;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import team.wego.wegobackend.notification.application.SseEmitterService;
import team.wego.wegobackend.notification.application.dto.response.NotificationFollowResponse;
import team.wego.wegobackend.notification.domain.Notification;
import team.wego.wegobackend.notification.repository.NotificationRepository;
import team.wego.wegobackend.user.application.event.UserFollowEvent;
import team.wego.wegobackend.user.domain.User;
import team.wego.wegobackend.user.exception.UserNotFoundException;
import team.wego.wegobackend.user.repository.UserRepository;

@Component
@RequiredArgsConstructor
public class FollowEventListener {

    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final SseEmitterService sseEmitterService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleFollowEvent(UserFollowEvent event) {

        User follower = userRepository.findById(event.followerId())
            .orElseThrow(UserNotFoundException::new);

        User follow = userRepository.findById(event.followId())
            .orElseThrow(UserNotFoundException::new);

        Notification notification = Notification.createFollowNotification(follow, follower);

        Notification saveNotification = notificationRepository.save(notification);

        notificationRepository.flush();

        sseEmitterService.sendNotificationIfConnected(follow.getId(), new NotificationFollowResponse(saveNotification, follower));
    }
}
