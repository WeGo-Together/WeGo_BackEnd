package team.wego.wegobackend.group.v2.domain.repository;


import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import team.wego.wegobackend.group.v2.domain.entity.GroupV2;

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
}
