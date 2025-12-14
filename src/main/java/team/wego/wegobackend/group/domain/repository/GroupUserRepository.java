package team.wego.wegobackend.group.domain.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import team.wego.wegobackend.group.domain.entity.Group;
import team.wego.wegobackend.group.domain.entity.GroupUser;
import team.wego.wegobackend.group.domain.entity.GroupUserStatus;
import team.wego.wegobackend.user.domain.User;

public interface GroupUserRepository extends JpaRepository<GroupUser, Long> {

    Optional<GroupUser> findByGroupAndUser(Group group, User user);

    long countByGroupAndStatus(Group group, GroupUserStatus status);

    List<GroupUser> findByGroupAndStatusOrderByJoinedAtAsc(Group group, GroupUserStatus status);


    @Query("""
            select gu
            from GroupUser gu
            join fetch gu.user u
            where gu.group = :group
              and gu.status = :status
            """)
    List<GroupUser> findAllByGroupAndStatusFetchUser(
            @Param("group") Group group,
            @Param("status") GroupUserStatus status);
}
