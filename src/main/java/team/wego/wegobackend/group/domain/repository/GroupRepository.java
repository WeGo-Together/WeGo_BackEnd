package team.wego.wegobackend.group.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import team.wego.wegobackend.group.domain.entity.Group;

public interface GroupRepository extends JpaRepository<Group, Long> {

}
