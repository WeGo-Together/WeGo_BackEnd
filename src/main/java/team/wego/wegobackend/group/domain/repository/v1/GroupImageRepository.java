package team.wego.wegobackend.group.domain.repository.v1;


import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import team.wego.wegobackend.group.domain.entity.Group;
import team.wego.wegobackend.group.domain.entity.GroupImage;

public interface GroupImageRepository extends JpaRepository<GroupImage, Long> {

    Optional<GroupImage> findByGroupAndImageUrl(Group group, String imageUrl);

    void deleteByGroupAndImageUrl(Group group, String imageUrl);

    boolean existsByGroupAndImageUrl(Group group, String imageUrl);
}
