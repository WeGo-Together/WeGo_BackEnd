package team.wego.wegobackend.group.v2.application.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import team.wego.wegobackend.auth.exception.UserNotFoundException;
import team.wego.wegobackend.group.domain.exception.GroupErrorCode;
import team.wego.wegobackend.group.domain.exception.GroupException;
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
public class GroupJoinNotificationHandler {

    private final UserRepository userRepository;
    private final GroupV2Repository groupV2Repository;
    private final NotificationRepository notificationRepository;
    private final SseEmitterService sseEmitterService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handle(Long groupId, Long hostUserId, Long joinerUserId) {
        User actor = userRepository.findById(joinerUserId)
                .orElseThrow(() -> new GroupException(GroupErrorCode.GROUP_USER_NOT_FOUND, joinerUserId));

        User receiver = userRepository.findById(hostUserId)
                .orElseThrow(() -> new GroupException(GroupErrorCode.HOST_USER_NOT_FOUND, hostUserId));

        GroupV2 group = groupV2Repository.findById(groupId)
                .orElseThrow(() -> new GroupException(GroupErrorCode.GROUP_NOT_FOUND_BY_ID, groupId));


        Notification saved = notificationRepository.save(
                Notification.createGroupJoinNotification(receiver, actor, group)
        );

        sseEmitterService.sendNotification(receiver.getId(), NotificationEvent.of(saved, actor, group));
    }
}
