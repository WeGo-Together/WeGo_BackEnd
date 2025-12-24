package team.wego.wegobackend.group.v2.application.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import team.wego.wegobackend.auth.exception.UserNotFoundException;
import team.wego.wegobackend.group.v2.application.event.NotificationEvent;
import team.wego.wegobackend.group.v2.domain.entity.GroupV2;
import team.wego.wegobackend.group.v2.domain.repository.GroupV2Repository;
import team.wego.wegobackend.notification.application.SseEmitterService;
import team.wego.wegobackend.notification.domain.Notification;
import team.wego.wegobackend.notification.repository.NotificationRepository;
import team.wego.wegobackend.user.domain.User;
import team.wego.wegobackend.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class GroupJoinRequestNotificationHandler {

    private final UserRepository userRepository;
    private final GroupV2Repository groupV2Repository;

    private final NotificationRepository notificationRepository;
    private final SseEmitterService sseEmitterService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handle(Long groupId, Long hostUserId, Long requesterUserId) {

        User host = userRepository.findById(hostUserId).orElseThrow(UserNotFoundException::new);
        User requester = userRepository.findById(requesterUserId)
                .orElseThrow(UserNotFoundException::new);
        GroupV2 group = groupV2Repository.findById(groupId).orElseThrow(UserNotFoundException::new);

        Notification notification =
                Notification.createGroupJoinRequestNotification(host, requester, group);

        Notification saved = notificationRepository.save(notification);

        NotificationEvent dto = NotificationEvent.of(saved, requester, group);
        sseEmitterService.sendNotification(host.getId(), dto);
    }
}
