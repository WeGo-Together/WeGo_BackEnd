package team.wego.wegobackend.group.domain.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import team.wego.wegobackend.group.domain.entity.Group;

public interface GroupRepository extends JpaRepository<Group, Long> {

    Optional<Group> findByIdAndDeletedAtIsNull(Long id);

}
