package team.wego.wegobackend.group.v2.domain.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import team.wego.wegobackend.group.v2.domain.entity.GroupUserV2;

public interface GroupUserV2Repository extends JpaRepository<GroupUserV2, Long> {

    @Query("""
                select gu
                from GroupUserV2 gu
                  join fetch gu.user u
                where gu.group.id = :groupId
            """)
    List<GroupUserV2> findAllByGroupIdWithUser(@Param("groupId") Long groupId);
}
