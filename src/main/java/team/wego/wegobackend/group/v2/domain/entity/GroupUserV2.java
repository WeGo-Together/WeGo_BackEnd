package team.wego.wegobackend.group.v2.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import team.wego.wegobackend.common.entity.BaseTimeEntity;
import team.wego.wegobackend.group.domain.exception.GroupErrorCode;
import team.wego.wegobackend.group.domain.exception.GroupException;
import team.wego.wegobackend.user.domain.User;

@Getter(AccessLevel.PUBLIC)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "v2_group_users",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_group_user", columnNames = {"group_id", "user_id"}
        )
)
@Entity
public class GroupUserV2 extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private GroupV2 group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "group_role", nullable = false, length = 20)
    private GroupUserV2Role groupRole;

    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt;

    @Column(name = "left_at")
    private LocalDateTime leftAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "group_user_status", nullable = false, length = 20)
    private GroupUserV2Status status;

    private GroupUserV2(User user, GroupUserV2Role groupRole) {
        this.user = user;
        this.groupRole = groupRole;
        this.joinedAt = LocalDateTime.now();
        this.status = GroupUserV2Status.ATTEND;
        this.leftAt = null;
    }

    public static GroupUserV2 create(GroupV2 group, User user, GroupUserV2Role role) {
        GroupUserV2 groupUser = new GroupUserV2(user, role);
        group.addUser(groupUser);
        return groupUser;
    }

    public void reAttend() {
        if (this.status == GroupUserV2Status.BANNED) {
            throw new GroupException(GroupErrorCode.GROUP_BANNED_USER);
        }

        this.status = GroupUserV2Status.ATTEND;
        this.joinedAt = LocalDateTime.now();
        this.leftAt = null;
    }

    public void leave() {
        if (this.status != GroupUserV2Status.ATTEND) {
            return;
        }

        this.status = GroupUserV2Status.LEFT;
        this.leftAt = LocalDateTime.now();
    }

    public void kick() {
        if (this.status != GroupUserV2Status.ATTEND) {
            return;
        }
        this.status = GroupUserV2Status.KICKED;
        this.leftAt = LocalDateTime.now();
    }

    public void ban() {
        this.status = GroupUserV2Status.BANNED;
        this.leftAt = LocalDateTime.now();
    }

    void assignTo(GroupV2 group) {
        this.group = group;
    }

    void unassign() {
        this.group = null;
    }
}

