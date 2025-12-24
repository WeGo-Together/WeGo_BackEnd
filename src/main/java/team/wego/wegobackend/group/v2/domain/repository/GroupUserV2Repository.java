package team.wego.wegobackend.group.v2.domain.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import team.wego.wegobackend.group.v2.domain.entity.GroupUserV2;
import team.wego.wegobackend.group.v2.domain.entity.GroupUserV2Status;

public interface GroupUserV2Repository extends JpaRepository<GroupUserV2, Long> {

    @Query("""
                select gu
                from GroupUserV2 gu
                  join fetch gu.user u
                where gu.group.id = :groupId
            """)
    List<GroupUserV2> findAllByGroupIdWithUser(@Param("groupId") Long groupId);

    Optional<GroupUserV2> findByGroupIdAndUserId(Long groupId, Long userId);

    long countByGroupIdAndStatus(Long groupId, GroupUserV2Status status);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from GroupUserV2 gu where gu.group.id = :groupId")
    int deleteByGroupId(@Param("groupId") Long groupId);


    @Query("""
        select gu.user.id
        from GroupUserV2 gu
        where gu.group.id = :groupId
          and gu.status = :status
    """)
    List<Long> findUserIdsByGroupIdAndStatus(@Param("groupId") Long groupId,
            @Param("status") GroupUserV2Status status);
}
