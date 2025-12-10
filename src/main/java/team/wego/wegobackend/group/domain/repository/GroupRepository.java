package team.wego.wegobackend.group.domain.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import team.wego.wegobackend.group.domain.entity.Group;

public interface GroupRepository extends JpaRepository<Group, Long> {

    Optional<Group> findByIdAndDeletedAtIsNull(Long id);

    @Query(value = """
            SELECT DISTINCT g.*
            FROM v1_groups g
            LEFT JOIN v1_group_tags gt ON gt.group_id = g.group_id
            LEFT JOIN v1_tags t ON t.tag_id = gt.tag_id
            WHERE g.deleted_at IS NULL
              AND (:cursor IS NULL OR g.group_id < :cursor)
              AND (
                  :keyword IS NULL
                  OR :keyword = ''
                  OR g.title LIKE CONCAT('%', :keyword, '%')
                  OR g.location LIKE CONCAT('%', :keyword, '%')
                  OR t.name LIKE CONCAT('%', :keyword, '%')
              )
            ORDER BY g.group_id DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<Group> findGroupsWithKeywordAndCursor(
            @Param("keyword") String keyword,
            @Param("cursor") Long cursor,
            @Param("limit") int limit
    );
}
