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
            throw new GroupException(GroupErrorCode.GROUP_NOT_ATTEND_STATUS);
        }

        this.status = GroupUserV2Status.LEFT;
        this.leftAt = LocalDateTime.now();
    }

    public void cancelRequest() {
        if (this.status != GroupUserV2Status.PENDING) {
            throw new GroupException(
                    GroupErrorCode.GROUP_NOT_PENDING_STATUS,
                    this.group.getId(),
                    this.user.getId(),
                    this.status.name()
            );
        }

        this.status = GroupUserV2Status.CANCELLED;
        this.leftAt = LocalDateTime.now();
    }

    public void kick() {
        if (this.status != GroupUserV2Status.ATTEND) {
            throw new GroupException(GroupErrorCode.GROUP_NOT_ATTEND_STATUS);
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

    public static GroupUserV2 createPending(GroupV2 group, User user) {
        GroupUserV2 groupUserV2 = new GroupUserV2(user, GroupUserV2Role.MEMBER);
        groupUserV2.status = GroupUserV2Status.PENDING;   // 신청 상태로 시작
        groupUserV2.joinedAt = LocalDateTime.now();
        groupUserV2.leftAt = null;
        group.addUser(groupUserV2);
        return groupUserV2;
    }

    public void requestJoin() {
        if (this.status == GroupUserV2Status.BANNED) {
            throw new GroupException(GroupErrorCode.GROUP_BANNED_USER);
        }
        // 이미 ATTEND, PENDING이면 상위에서 걸러도 되고 여기서 방어해도 가능하다고 판단
        this.status = GroupUserV2Status.PENDING;
        this.joinedAt = LocalDateTime.now();
        this.leftAt = null;
    }

    public void leaveOrCancel() {
        switch (this.status) {
            case ATTEND -> this.leave();
            case PENDING -> this.cancelRequest();
            case BANNED -> throw new GroupException(GroupErrorCode.GROUP_BANNED_USER);
            case KICKED -> throw new GroupException(
                    GroupErrorCode.GROUP_KICKED_USER,
                    this.group.getId(),
                    this.user.getId()
            );
            case REJECTED -> throw new GroupException(
                    GroupErrorCode.GROUP_REJECTED_USER,
                    this.group.getId(),
                    this.user.getId()
            );
            case LEFT -> throw new GroupException(
                    GroupErrorCode.ALREADY_LEFT_GROUP,
                    this.group.getId(),
                    this.user.getId()
            );
            case CANCELLED -> throw new GroupException(
                    GroupErrorCode.ALREADY_CANCELLED_JOIN_REQUEST,
                    this.group.getId(),
                    this.user.getId()
            );
            default -> throw new GroupException(
                    GroupErrorCode.GROUP_USER_STATUS_NOT_ALLOWED_TO_LEAVE,
                    this.group.getId(),
                    this.user.getId(),
                    this.status.name()
            );
        }
    }
}
