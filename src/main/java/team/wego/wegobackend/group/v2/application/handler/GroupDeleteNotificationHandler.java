package team.wego.wegobackend.group.v2.application.handler;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import team.wego.wegobackend.group.v2.domain.entity.GroupV2;
import team.wego.wegobackend.group.v2.domain.repository.GroupV2Repository;
import team.wego.wegobackend.notification.application.dispatcher.NotificationDispatcher;
import team.wego.wegobackend.notification.domain.Notification;
import team.wego.wegobackend.user.domain.User;
import team.wego.wegobackend.user.repository.UserRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class GroupDeleteNotificationHandler {

    private final UserRepository userRepository;
    private final GroupV2Repository groupV2Repository;
    private final NotificationDispatcher notificationDispatcher;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handle(Long groupId, Long hostUserId, List<Long> attendeeUserIds) {
        if (attendeeUserIds == null || attendeeUserIds.isEmpty()) return;

        User host = userRepository.findById(hostUserId).orElseThrow();
        GroupV2 group = groupV2Repository.findById(groupId).orElseThrow();

        List<Notification> notifications = new ArrayList<>(attendeeUserIds.size());

        for (Long receiverId : attendeeUserIds) {
            if (receiverId.equals(hostUserId)) continue; // 호스트 자신 제외(원하면 제거)
            User receiver = userRepository.getReferenceById(receiverId);

            notifications.add(Notification.createGroupDeleteNotification(receiver, host, group));
        }

        notificationDispatcher.dispatch(notifications, host, group);
    }
}

