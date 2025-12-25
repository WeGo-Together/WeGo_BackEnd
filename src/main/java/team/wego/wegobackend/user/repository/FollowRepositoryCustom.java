package team.wego.wegobackend.user.repository;

import java.util.List;
import team.wego.wegobackend.user.application.dto.response.FollowResponse;
import team.wego.wegobackend.user.domain.User;
import team.wego.wegobackend.user.repository.query.FollowerNotifyRow;

public interface FollowRepositoryCustom {

    List<FollowResponse> findFollowingList(
            Long followerId,
            Long cursorFollowId,
            int size
    );

    List<FollowerNotifyRow> findFollowersForNotify(Long followeeId, Long cursorFollowId, int size);

}
