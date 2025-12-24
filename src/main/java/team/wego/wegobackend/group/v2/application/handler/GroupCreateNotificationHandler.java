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
import team.wego.wegobackend.user.repository.FollowRepository;
import team.wego.wegobackend.user.repository.UserRepository;
import team.wego.wegobackend.user.repository.query.FollowerNotifyRow;

@Slf4j
@Service
@RequiredArgsConstructor
public class GroupCreateNotificationHandler {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;
    private final GroupV2Repository groupV2Repository;

    private final NotificationDispatcher notificationDispatcher;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handle(Long groupId, Long hostUserId) {
        log.info("[NOTI] start handle. groupId={}, hostId={}", groupId, hostUserId);

        List<FollowerNotifyRow> test = followRepository.findFollowersForNotify(hostUserId, null, 10);
        log.info("[NOTI] follower rows size={}", test.size());

        User host = userRepository.findById(hostUserId)
                .orElseThrow();

        GroupV2 group = groupV2Repository.findById(groupId)
                .orElseThrow();

        Long cursor = null;
        final int size = 500;

        while (true) {
            List<FollowerNotifyRow> rows =
                    followRepository.findFollowersForNotify(
                            hostUserId, cursor, size
                    );

            if (rows.isEmpty()) {
                break;
            }

            List<Notification> notifications = new ArrayList<>(rows.size());

            for (FollowerNotifyRow row : rows) {
                User receiver = userRepository.getReferenceById(row.userId());
                notifications.add(
                        Notification.createGroupCreateNotification(
                                receiver,
                                host,
                                group
                        )
                );
            }

            // 공통 디스패처 호출
            notificationDispatcher.dispatch(notifications, host, group);

            cursor = rows.getLast().followId();
        }
    }
}

