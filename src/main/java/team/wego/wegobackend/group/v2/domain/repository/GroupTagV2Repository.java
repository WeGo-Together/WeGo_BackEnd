package team.wego.wegobackend.group.v2.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import team.wego.wegobackend.group.v2.domain.entity.GroupTagV2;

public interface GroupTagV2Repository extends JpaRepository<GroupTagV2, Long> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from GroupTagV2 gt where gt.group.id = :groupId")
    int deleteByGroupId(@Param("groupId") Long groupId);
}

