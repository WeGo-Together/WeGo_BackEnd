package team.wego.wegobackend.group.v2.application.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import team.wego.wegobackend.group.v2.application.event.NotificationEvent;
import team.wego.wegobackend.group.v2.domain.entity.GroupV2;
import team.wego.wegobackend.group.v2.domain.repository.GroupV2Repository;
import team.wego.wegobackend.notification.domain.Notification;
import team.wego.wegobackend.notification.repository.NotificationRepository;
import team.wego.wegobackend.notification.application.SseEmitterService;
import team.wego.wegobackend.user.domain.User;
import team.wego.wegobackend.user.exception.UserNotFoundException;
import team.wego.wegobackend.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class GroupJoinDecisionNotificationHandler {

    private final UserRepository userRepository;
    private final GroupV2Repository groupV2Repository;

    private final NotificationRepository notificationRepository;
    private final SseEmitterService sseEmitterService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleApproved(Long groupId, Long approverUserId, Long targetUserId) {
        handle(groupId, approverUserId, targetUserId, true);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleRejected(Long groupId, Long approverUserId, Long targetUserId) {
        handle(groupId, approverUserId, targetUserId, false);
    }

    private void handle(Long groupId, Long approverUserId, Long targetUserId, boolean approved) {
        User actor = userRepository.findById(approverUserId)
                .orElseThrow(UserNotFoundException::new);
        User receiver = userRepository.findById(targetUserId)
                .orElseThrow(UserNotFoundException::new);
        GroupV2 group = groupV2Repository.findById(groupId)
                .orElseThrow(UserNotFoundException::new);

        Notification notification = approved
                ? Notification.createGroupJoinApprovedNotification(receiver, actor, group)
                : Notification.createGroupJoinRejectedNotification(receiver, actor, group);

        Notification saved = notificationRepository.save(notification);

        NotificationEvent dto = NotificationEvent.of(saved, actor, group);
        sseEmitterService.sendNotification(receiver.getId(), dto);
    }
}
