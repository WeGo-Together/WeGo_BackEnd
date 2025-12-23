package team.wego.wegobackend.notification.application;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.wego.wegobackend.auth.exception.UserNotFoundException;
import team.wego.wegobackend.notification.application.dto.response.NotificationListResponse;
import team.wego.wegobackend.notification.application.dto.response.NotificationResponse;
import team.wego.wegobackend.notification.domain.Notification;
import team.wego.wegobackend.notification.exception.NotificationAccessDeniedException;
import team.wego.wegobackend.notification.exception.NotificationNotFoundException;
import team.wego.wegobackend.notification.repository.NotificationRepository;
import team.wego.wegobackend.user.domain.User;
import team.wego.wegobackend.user.repository.UserRepository;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {

    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;

    @Transactional(readOnly = true)
    public NotificationListResponse notificationList(Long userId, Long cursor, Integer size) {

        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);

        List<NotificationResponse> result = notificationRepository.findNotificationList(
            user.getId(), cursor, size);
        Long nextCursor = result.isEmpty() ? null : result.getLast().getId();

        return new NotificationListResponse(result, nextCursor);
    }

    @Transactional(readOnly = true)
    public Long unreadNotificationCount(Long userId) {

        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);

        Long unreadCount = notificationRepository.countUnread(user.getId());

        return unreadCount;
    }

    public void readNotification(Long userId, Long notificationId) {

        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);

        Notification notification = notificationRepository.findById(notificationId).orElseThrow(
            NotificationNotFoundException::new);

        if(!user.getId().equals(notification.getReceiver().getId())) {
            log.debug("login User -> {}, receiver User -> {}", user.getId(),
                notification.getReceiver().getId());
            throw new NotificationAccessDeniedException();
        }

        notification.markAsRead();
    }

    public void readAllNotification(Long userId) {

        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);

        int resultCount = notificationRepository.markAllAsRead(user.getId());
    }
}
