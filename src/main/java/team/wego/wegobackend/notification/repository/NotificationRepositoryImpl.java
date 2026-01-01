package team.wego.wegobackend.notification.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import team.wego.wegobackend.group.v2.domain.entity.QGroupV2;
import team.wego.wegobackend.notification.application.dto.response.NotificationItemResponse;
import team.wego.wegobackend.notification.domain.QNotification;
import team.wego.wegobackend.user.domain.QUser;


@RequiredArgsConstructor
public class NotificationRepositoryImpl implements NotificationRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<NotificationItemResponse> findNotificationList(
            Long userId,
            Long cursorNotificationId,
            int size
    ) {

        QNotification notification = QNotification.notification;
        QUser user = QUser.user;
        QGroupV2 groupV2 = QGroupV2.groupV2;

        return jpaQueryFactory
                .select(Projections.constructor(
                        NotificationItemResponse.class,
                        notification.id,
                        notification.actor.id,
                        notification.actor.nickName,
                        groupV2.id,
                        groupV2.title,
                        notification.type,
                        notification.message,
                        notification.readAt,
                        notification.createdAt
                ))
                .from(notification)
                .leftJoin(notification.actor, user)
                .leftJoin(groupV2).on(
                        notification.relatedType.eq("GROUP")
                                .and(notification.relatedId.eq(groupV2.id))
                )
                .where(notification.receiver.id.eq(userId), ltCursor(cursorNotificationId))
                .orderBy(notification.id.desc())
                .limit(size)
                .fetch();
    }

    private BooleanExpression ltCursor(Long cursorNotificationId) {
        return cursorNotificationId == null ? null
                : QNotification.notification.id.lt(cursorNotificationId);
    }
}

