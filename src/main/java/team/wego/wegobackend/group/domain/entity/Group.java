package team.wego.wegobackend.group.domain.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import team.wego.wegobackend.common.entity.BaseTimeEntity;
import team.wego.wegobackend.user.domain.User;

@Getter(AccessLevel.PUBLIC)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "v1_groups")
@Entity
public class Group extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "group_id", nullable = false, unique = true)
    private Long id;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "location", nullable = false, length = 100)
    private String location;

    @Column(name = "location_detail", length = 255)
    private String locationDetail;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "description", nullable = false, length = 1000)
    private String description;

    @Column(name = "max_participants", nullable = false)
    private Integer maxParticipants;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "host_id", nullable = false)
    private User host;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GroupImage> images = new ArrayList<>();

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GroupTag> groupTags = new ArrayList<>();

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GroupUser> users = new ArrayList<>();

    public static Group create(
            String title,
            String location,
            String locationDetail,
            LocalDateTime startTime,
            LocalDateTime endTime,
            String description,
            Integer maxParticipants,
            User host
    ) {
        Group group = new Group();
        group.title = title;
        group.location = location;
        group.locationDetail = locationDetail;
        group.startTime = startTime;
        group.endTime = endTime;
        group.description = description;
        group.maxParticipants = maxParticipants;
        group.host = host;
        return group;
    }

    // 연관관계 편의 메서드들
    public void addImage(GroupImage image) {
        this.images.add(image);
    }

    public void addUser(GroupUser groupUser) {
        this.users.add(groupUser);
    }

    public void addTag(GroupTag groupTag) {
        this.groupTags.add(groupTag);
    }

    public void update(
            String title,
            String location,
            String locationDetail,
            LocalDateTime startTime,
            LocalDateTime endTime,
            String description,
            Integer maxParticipants
    ) {
        this.title = title;
        this.location = location;
        this.locationDetail = locationDetail;
        this.startTime = startTime;
        this.endTime = endTime;
        this.description = description;
        this.maxParticipants = maxParticipants;
    }
}
