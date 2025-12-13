package team.wego.wegobackend.user.repository;

import java.util.List;
import team.wego.wegobackend.user.application.dto.response.FollowResponse;

public interface FollowRepositoryCustom {

    List<FollowResponse> findFollowingList(
        Long followerId,
        Long cursorFollowId,
        int size
        );
}
