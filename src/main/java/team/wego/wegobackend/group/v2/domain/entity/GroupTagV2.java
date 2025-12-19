package team.wego.wegobackend.group.v2.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import team.wego.wegobackend.common.entity.BaseTimeEntity;
import team.wego.wegobackend.tag.domain.entity.Tag;

@Getter(AccessLevel.PUBLIC)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "v2_group_tags",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_group_id_tag_id", columnNames = {"group_id", "tag_id"})
        }
)
@Entity
public class GroupTagV2 extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "group_tag_id", nullable = false, updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private GroupV2 group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id", nullable = false)
    private Tag tag;

    private GroupTagV2(GroupV2 group, Tag tag) {
        this.group = group;
        this.tag = tag;
    }

    public static GroupTagV2 create(GroupV2 group, Tag tag) {
        GroupTagV2 groupTag = new GroupTagV2(group, tag);
        group.addTag(groupTag);
        return groupTag;
    }

    void assignTo(GroupV2 groupV2) {
        this.group = groupV2;
    }
}


