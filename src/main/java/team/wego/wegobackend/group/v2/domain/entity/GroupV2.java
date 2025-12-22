package team.wego.wegobackend.group.v2.domain.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
import team.wego.wegobackend.group.domain.exception.GroupErrorCode;
import team.wego.wegobackend.group.domain.exception.GroupException;
import team.wego.wegobackend.user.domain.User;

@Getter(AccessLevel.PUBLIC)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "v2_groups")
@Entity
public class GroupV2 extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "group_id", nullable = false, unique = true)
    private Long id;

    @Column(name = "title", nullable = false, length = 50)
    private String title;

    @Column(name = "description", nullable = false, length = 300)
    private String description;

    @Embedded
    private GroupV2Address address;

    @Enumerated(EnumType.STRING)
    @Column(name = "group_status", nullable = false, length = 30)
    private GroupV2Status status;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "max_participants", nullable = false)
    private Integer maxParticipants;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "host_id", nullable = false)
    private User host;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "join_policy", nullable = false, length = 30)
    private GroupV2JoinPolicy joinPolicy;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GroupImageV2> images = new ArrayList<>();

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GroupTagV2> groupTags = new ArrayList<>();

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GroupUserV2> users = new ArrayList<>();

    public static GroupV2 create(
            String title,
            GroupV2Address address,
            LocalDateTime startTime,
            LocalDateTime endTime,
            String description,
            Integer maxParticipants,
            User host,
            GroupV2JoinPolicy joinPolicy
    ) {
        GroupV2 group = new GroupV2();
        group.title = title;
        group.address = address;
        group.startTime = startTime;
        group.endTime = endTime;
        group.description = description;
        group.maxParticipants = maxParticipants;
        group.host = host;
        group.joinPolicy = (joinPolicy == null) ? GroupV2JoinPolicy.INSTANT : joinPolicy;
        group.status = GroupV2Status.RECRUITING;
        return group;
    }

    public void changeStatus(GroupV2Status status) {
        if (status == null) {
            throw new GroupException(GroupErrorCode.GROUP_STATUS_REQUIRED);
        }
        if (this.status == null) {
            this.status = GroupV2Status.RECRUITING;
        }
        if (!this.status.canTransitionTo(status)) {
            throw new GroupException(GroupErrorCode.GROUP_STATUS_TRANSFER_IMPOSSIBLE, this.status,
                    status);
        }
        this.status = status;
    }

    public void close() {
        changeStatus(GroupV2Status.CLOSED);
    }

    public void cancel() {
        changeStatus(GroupV2Status.CANCELLED);
    }

    public void finish() {
        changeStatus(GroupV2Status.FINISHED);
    }

    public void addImage(GroupImageV2 image) {
        this.images.add(image);
        image.assignTo(this);
    }

    public void removeImage(GroupImageV2 image) {
        this.images.remove(image);
        image.unassign();
    }

    public void addUser(GroupUserV2 groupUser) {
        this.users.add(groupUser);
        groupUser.assignTo(this);
    }

    public void removeUser(GroupUserV2 groupUser) {
        this.users.remove(groupUser);
        groupUser.unassign();
    }

    public void addTag(GroupTagV2 groupTag) {
        this.groupTags.add(groupTag);
        groupTag.assignTo(this);
    }

    public void removeTag(GroupTagV2 groupTag) {
        this.groupTags.remove(groupTag);
        groupTag.assignTo(null);
    }

    public void assertUpdatable() {
        if (this.deletedAt != null) {
            throw new GroupException(GroupErrorCode.GROUP_DELETED);
        }
        if (this.status == GroupV2Status.CANCELLED || this.status == GroupV2Status.FINISHED) {
            throw new GroupException(GroupErrorCode.GROUP_CANNOT_UPDATE_IN_STATUS, this.status.name());
        }
    }

    public void changeTitle(String title) {
        assertUpdatable();

        if (title == null || title.isBlank()) {
            throw new GroupException(GroupErrorCode.GROUP_TITLE_REQUIRED);
        }
        String trimmed = title.trim();
        if (trimmed.length() > 50) {
            throw new GroupException(GroupErrorCode.GROUP_TITLE_TOO_LONG);
        }
        this.title = trimmed;
    }

    public void changeDescription(String description) {
        assertUpdatable();

        if (description == null || description.isBlank()) {
            throw new GroupException(GroupErrorCode.GROUP_DESCRIPTION_REQUIRED);
        }
        String trimmed = description.trim();
        if (trimmed.length() > 300) {
            throw new GroupException(GroupErrorCode.GROUP_DESCRIPTION_TOO_LONG);
        }
        this.description = trimmed;
    }

    public void changeAddress(GroupV2Address address) {
        assertUpdatable();

        if (address == null) {
            throw new GroupException(GroupErrorCode.LOCATION_REQUIRED);
        }
        this.address = address;
    }


    // 시간은 "부분 수정"을 위해 start/end 단건 변경도 지원하되, 항상 최종 상태에서 start < end 불변식을 만족하자.
    public void changeStartTime(LocalDateTime startTime) {
        assertUpdatable();

        if (startTime == null) {
            throw new GroupException(GroupErrorCode.GROUP_TIME_REQUIRED);
        }
        LocalDateTime newStart = startTime;
        LocalDateTime newEnd = this.endTime; // end는 nullable일 수 있음
        validateTimeRange(newStart, newEnd);
        this.startTime = newStart;
    }

    public void changeEndTime(LocalDateTime endTime) {
        assertUpdatable();

        // endTime을 null 허용할지 정책 -> 안하는 중!
        LocalDateTime newStart = this.startTime;
        LocalDateTime newEnd = endTime;
        validateTimeRange(newStart, newEnd);
        this.endTime = newEnd;
    }

    public void changeTime(LocalDateTime startTime, LocalDateTime endTime) {
        assertUpdatable();

        if (startTime == null) {
            throw new GroupException(GroupErrorCode.GROUP_TIME_REQUIRED);
        }
        validateTimeRange(startTime, endTime);
        this.startTime = startTime;
        this.endTime = endTime;
    }

    private void validateTimeRange(LocalDateTime start, LocalDateTime end) {
        // endTime을 필수 아닌 상태다.
        if (end == null) {
            return;
        }
        if (!start.isBefore(end)) {
            throw new GroupException(GroupErrorCode.GROUP_TIME_INVALID_RANGE);
        }
    }

    public void changeMaxParticipants(Integer maxParticipants) {
        assertUpdatable();

        if (maxParticipants == null || maxParticipants <= 0) {
            throw new GroupException(GroupErrorCode.INVALID_MAX_PARTICIPANTS);
        }
        this.maxParticipants = maxParticipants;
    }

    // 사용할 지 안할지 아직 잘 모름
    public void softDelete() {
        if (this.deletedAt != null) {
            return;
        }
        this.deletedAt = LocalDateTime.now();
    }
}


