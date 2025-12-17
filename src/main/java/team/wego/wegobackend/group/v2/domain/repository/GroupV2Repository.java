package team.wego.wegobackend.group.domain.repository.v2;


import org.springframework.data.jpa.repository.JpaRepository;
import team.wego.wegobackend.group.domain.entity.Group;

public interface GroupV2Repository extends JpaRepository<Group, Long> {

}
