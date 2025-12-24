package team.wego.wegobackend.user.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import team.wego.wegobackend.user.application.dto.response.FollowResponse;
import team.wego.wegobackend.user.application.dto.response.QFollowResponse;
import team.wego.wegobackend.user.domain.QFollow;
import team.wego.wegobackend.user.domain.QUser;
import team.wego.wegobackend.user.domain.User;
import team.wego.wegobackend.user.repository.query.FollowerNotifyRow;

@RequiredArgsConstructor
public class FollowRepositoryImpl implements FollowRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<FollowResponse> findFollowingList(
            Long followerId,
            Long cursorFollowId,
            int size
    ) {

        QFollow follow = QFollow.follow;
        QUser user = QUser.user;

        return jpaQueryFactory
                .select(new QFollowResponse(
                        follow.id,
                        user.id,
                        user.profileImage,
                        user.nickName,
                        user.profileMessage
                ))
                .from(follow)
                .join(follow.followee, user)
                .where(
                        follow.follower.id.eq(followerId),
                        itCursor(cursorFollowId)
                )
                .orderBy(follow.id.desc())  //최신 팔로우 순
                .limit(size)
                .fetch();
    }

    private BooleanExpression itCursor(Long cursorFollowId) {
        return cursorFollowId == null
                ? null
                : QFollow.follow.id.lt(cursorFollowId);
    }

    @Override
    public List<FollowerNotifyRow> findFollowersForNotify(
            Long followeeId,
            Long cursorFollowId,
            int size
    ) {
        QFollow follow = QFollow.follow;
        QUser user = QUser.user;

        return jpaQueryFactory
                .select(Projections.constructor(
                        FollowerNotifyRow.class,
                        follow.id,
                        user.id,
                        user.nickName,
                        user.profileImage
                ))
                .from(follow)
                .join(follow.follower, user)
                .where(
                        follow.followee.id.eq(followeeId),
                        cursorFollowId == null ? null : follow.id.lt(cursorFollowId),
                        user.notificationEnabled.isTrue(),
                        user.deleted.isFalse()
                )
                .orderBy(follow.id.desc())
                .limit(size)
                .fetch();
    }
}
