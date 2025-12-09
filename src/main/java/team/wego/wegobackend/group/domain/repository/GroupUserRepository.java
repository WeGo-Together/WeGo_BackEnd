package team.wego.wegobackend.group.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import team.wego.wegobackend.group.domain.entity.GroupUser;

public interface GroupUserRepository extends JpaRepository<GroupUser, Long> {

}
