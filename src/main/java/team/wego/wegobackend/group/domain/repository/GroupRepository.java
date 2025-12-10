package team.wego.wegobackend.group.domain.repository;

import java.time.LocalDateTime;
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

    /**
     * 내가 참여 중인 모임 (CURRENT)
     */
    @Query(value = """
            SELECT DISTINCT g.*
            FROM v1_groups g
            JOIN v1_group_users gu ON gu.group_id = g.group_id
            WHERE g.deleted_at IS NULL
              AND gu.user_id = :userId
              AND gu.group_user_status IN (:statuses)
              AND (:cursor IS NULL OR g.group_id < :cursor)
              AND (g.end_time IS NULL OR g.end_time >= :now)
            ORDER BY g.group_id DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<Group> findCurrentGroupsByUser(
            @Param("userId") Long userId,
            @Param("statuses") List<String> statuses,
            @Param("cursor") Long cursor,
            @Param("now") LocalDateTime now,
            @Param("limit") int limit
    );

    /**
     * 과거에 참여했던 모임 (PAST)
     */
    @Query(value = """
            SELECT DISTINCT g.*
            FROM v1_groups g
            JOIN v1_group_users gu ON gu.group_id = g.group_id
            WHERE g.deleted_at IS NULL
              AND gu.user_id = :userId
              AND gu.group_user_status IN (:statuses)
              AND (:cursor IS NULL OR g.group_id < :cursor)
              AND g.end_time IS NOT NULL
              AND g.end_time < :now
            ORDER BY g.group_id DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<Group> findPastGroupsByUser(
            @Param("userId") Long userId,
            @Param("statuses") List<String> statuses,
            @Param("cursor") Long cursor,
            @Param("now") LocalDateTime now,
            @Param("limit") int limit
    );

    /**
     * 내가 만든 모임 (MY_POST) – group_users 안타고 host 기준으로만
     */
    @Query(value = """
            SELECT DISTINCT g.*
            FROM v1_groups g
            WHERE g.deleted_at IS NULL
              AND g.host_id = :userId
              AND (:cursor IS NULL OR g.group_id < :cursor)
            ORDER BY g.group_id DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<Group> findMyPostGroupsByHost(
            @Param("userId") Long userId,
            @Param("cursor") Long cursor,
            @Param("limit") int limit
    );
}

