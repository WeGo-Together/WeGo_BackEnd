package team.wego.wegobackend.group.v2.domain.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import team.wego.wegobackend.group.v2.domain.entity.GroupImageV2;

public interface GroupImageV2Repository extends JpaRepository<GroupImageV2, Long> {

    @Query("""
                select distinct gi
                from GroupImageV2 gi
                  left join fetch gi.variants v
                where gi.group.id = :groupId
                order by gi.sortOrder asc
            """)
    List<GroupImageV2> findAllByGroupIdWithVariants(@Param("groupId") Long groupId);
}