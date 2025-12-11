package team.wego.wegobackend.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import team.wego.wegobackend.user.domain.Follow;

public interface FollowRepository extends JpaRepository<Follow, Long> {

    boolean existsByFollowerIdAndFolloweeId(Long followerId, Long followingId);
}
