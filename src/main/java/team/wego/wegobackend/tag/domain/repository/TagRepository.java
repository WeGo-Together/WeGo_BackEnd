package team.wego.wegobackend.tag.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import team.wego.wegobackend.tag.domain.entity.Tag;

public interface TagRepository extends JpaRepository<Tag, Long> {

}
