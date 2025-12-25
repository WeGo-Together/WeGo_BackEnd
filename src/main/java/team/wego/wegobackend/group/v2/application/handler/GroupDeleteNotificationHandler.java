package team.wego.wegobackend.group.v2.application.handler;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import team.wego.wegobackend.group.domain.exception.GroupErrorCode;
import team.wego.wegobackend.group.domain.exception.GroupException;
import team.wego.wegobackend.group.v2.application.event.GroupDeletedEvent;
import team.wego.wegobackend.notification.application.dispatcher.NotificationDispatcher;
import team.wego.wegobackend.notification.domain.Notification;
import team.wego.wegobackend.user.domain.User;
import team.wego.wegobackend.user.repository.UserRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class GroupDeleteNotificationHandler {

    private final UserRepository userRepository;
    private final NotificationDispatcher notificationDispatcher;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handle(GroupDeletedEvent event) {
        List<Long> ids = event.attendeeUserIds();
        int size = (ids == null ? 0 : ids.size());

        log.info("[GROUP_DELETE][HANDLER] start groupId={} hostId={} attendeeCount={}",
                event.groupId(), event.hostId(), size);

        if (ids == null || ids.isEmpty()) {
            return;
        }

        User host = userRepository.findById(event.hostId())
                .orElseThrow(() -> new GroupException(GroupErrorCode.HOST_USER_NOT_FOUND,
                        event.hostId()));

        List<Notification> notifications = new ArrayList<>(ids.size());
        for (Long receiverId : ids) {
            if (receiverId.equals(event.hostId())) {
                continue;
            }
            User receiver = userRepository.getReferenceById(receiverId);
            notifications.add(
                    Notification.createGroupDeleteNotification(receiver, host, event.groupId(),
                            event.groupTitle()));
        }

        log.info("[GROUP_DELETE][HANDLER] built notifications size={}", notifications.size());
        notificationDispatcher.dispatch(notifications, host, null);

        log.info("[GROUP_DELETE][HANDLER] done groupId={}", event.groupId());
    }
}


