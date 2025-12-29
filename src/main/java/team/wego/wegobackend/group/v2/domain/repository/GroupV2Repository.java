package team.wego.wegobackend.group.v2.domain.repository;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import team.wego.wegobackend.group.v2.domain.entity.GroupV2;
import team.wego.wegobackend.group.v2.domain.entity.GroupV2Status;

public interface GroupV2Repository extends JpaRepository<GroupV2, Long> {

    @Query("""
                select distinct g
                from GroupV2 g
                  join fetch g.host h
                  left join fetch g.groupTags gt
                  left join fetch gt.tag t
                where g.id = :groupId
            """)
    Optional<GroupV2> findGroupWithHostAndTags(@Param("groupId") Long groupId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
                update GroupV2 g
                   set g.status = 'FINISHED'
                 where g.deletedAt is null
                   and g.status in :targets
                   and g.startTime <= :now
            """)
    int bulkFinishByStartTime(
            @Param("now") LocalDateTime now,
            @Param("targets") List<GroupV2Status> targets
    );

    @Query("""
        select g.id
          from GroupV2 g
         where g.deletedAt is null
           and g.status = 'FINISHED'
           and g.startTime <= :threshold
         order by g.id asc
    """)
    List<Long> findFinishedExpiredGroupIdsByStartTime(
            @Param("threshold") LocalDateTime threshold,
            Pageable pageable
    );
}
