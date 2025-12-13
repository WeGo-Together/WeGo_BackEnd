package team.wego.wegobackend.user.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import team.wego.wegobackend.user.domain.Follow;

public interface FollowRepository extends JpaRepository<Follow, Long>, FollowRepositoryCustom {

    Optional<Follow> findByFollowerIdAndFolloweeId(Long followerId, Long followingId);
    boolean existsByFollowerIdAndFolloweeId(Long followerId, Long followingId);
}
