package team.wego.wegobackend.tag.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import team.wego.wegobackend.common.entity.BaseTimeEntity;

@Getter(AccessLevel.PUBLIC)
@NoArgsConstructor
@Table(name = "v1_tags",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_tags_name", columnNames = {"name"})
        })
@Entity
public class Tag extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tag_id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "name", nullable = false, unique = true, length = 50)
    private String name;

    public static Tag create(String name) {
        Tag tag = new Tag();
        tag.name = name;
        return tag;
    }
}
