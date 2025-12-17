package team.wego.wegobackend.group.v2.domain.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import team.wego.wegobackend.group.domain.entity.Group;

public interface GroupV2Repository extends JpaRepository<Group, Long> {

}
