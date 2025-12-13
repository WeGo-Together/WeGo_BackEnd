package team.wego.wegobackend.user.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import team.wego.wegobackend.user.application.dto.response.FollowResponse;
import team.wego.wegobackend.user.application.dto.response.QFollowResponse;
import team.wego.wegobackend.user.domain.QFollow;
import team.wego.wegobackend.user.domain.QUser;

@RequiredArgsConstructor
public class FollowRepositoryImpl implements FollowRepositoryCustom{

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
                ItCursor(cursorFollowId)
            )
            .orderBy(follow.id.desc())  //최신 팔로우 순
            .limit(size)
            .fetch();
    }

    private BooleanExpression ItCursor(Long cursorFollowId) {
        return cursorFollowId == null
            ? null
            : QFollow.follow.id.lt(cursorFollowId);
    }
}
