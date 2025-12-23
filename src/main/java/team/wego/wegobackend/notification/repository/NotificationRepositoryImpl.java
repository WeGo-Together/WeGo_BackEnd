package team.wego.wegobackend.notification.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import team.wego.wegobackend.notification.application.dto.response.NotificationResponse;
import team.wego.wegobackend.notification.application.dto.response.QNotificationResponse;
import team.wego.wegobackend.notification.domain.QNotification;
import team.wego.wegobackend.user.domain.QUser;

@RequiredArgsConstructor
public class NotificationRepositoryImpl implements NotificationRepositoryCustom{

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<NotificationResponse> findNotificationList(
        Long userId,
        Long cursorNotificationId,
        int size
    ) {

        QNotification notification = QNotification.notification;
        QUser user = QUser.user;

        /**
         * rivate Long id;
         *     private Long receiverId;
         *     private Long actorId;
         *     private String actorNickname;
         *     private String actorProfileImage;
         *     private NotificationType type;
         *     private String message;
         *     private Boolean isRead;
         *     private Long relatedId;
         *     private String relatedType;
         *     private String redirectUrl;
         *     private LocalDateTime createdAt;
         * */
        return jpaQueryFactory
            .select(new QNotificationResponse(
                notification.id,
                notification.receiver.id,
                notification.actor.id,
                notification.actor.nickName,
                notification.actor.profileImage,
                notification.type,
                notification.message,
                notification.readAt,
                notification.relatedId,
                notification.relatedType,
                notification.redirectUrl,
                notification.createdAt
            ))
            .from(notification)
            .join(notification.actor, user)
            .where(
                notification.receiver.id.eq(userId),
                ItCursor(cursorNotificationId)
            )
            .orderBy(notification.id.desc())
            .limit(size)
            .fetch();
    }

    private BooleanExpression ItCursor(Long cursorNotificationId) {
        return cursorNotificationId == null
            ? null
            : QNotification.notification.id.lt(cursorNotificationId);
    }
}
