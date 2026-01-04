package team.wego.wegobackend.user.repository;

import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import team.wego.wegobackend.user.domain.Follow;

public interface FollowRepository extends JpaRepository<Follow, Long>, FollowRepositoryCustom {

    Optional<Follow> findByFollowerIdAndFolloweeId(Long followerId, Long followingId);
    boolean existsByFollowerIdAndFolloweeId(Long followerId, Long followingId);

    void deleteByFollowerId(Long userId);

    void deleteByFolloweeId(Long userId);

    @Query("SELECT f.followee.id FROM Follow f WHERE f.follower.id = :followerId")
    Set<Long> findFolloweeIdsByFollowerId(@Param("followerId") Long followerId);
}
