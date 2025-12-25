package team.wego.wegobackend.notification.domain;

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
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import team.wego.wegobackend.common.entity.BaseTimeEntity;
import team.wego.wegobackend.group.v2.domain.entity.GroupV2;
import team.wego.wegobackend.notification.application.dto.NotificationType;
import team.wego.wegobackend.user.domain.User;

@Entity
@Table(name = "notifications")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id")
    private User actor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver")
    private User receiver;

    @Column(name = "message", nullable = false, length = 255)
    private String message;

    // 알림 타입 (FOLLOW, LIKE, COMMENT, MENTION 등)
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private NotificationType type;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    // 관련 리소스 ID (게시글 ID, 댓글 ID 등)
    @Column(name = "related_id")
    private Long relatedId;

    // 관련 리소스 타입 (POST, COMMENT 등)
    @Column(name = "related_type", length = 20)
    private String relatedType;

    // URL (클릭 시 이동할 경로)
    @Column(name = "redirect_url", length = 500)
    private String redirectUrl;

    @Builder
    public Notification(User receiver, User actor, NotificationType type,
            String message, Long relatedId, String relatedType,
            String redirectUrl) {
        this.receiver = receiver;
        this.actor = actor;
        this.type = type;
        this.message = message;
        this.relatedId = relatedId;
        this.relatedType = relatedType;
        this.redirectUrl = redirectUrl;
    }

    // 읽음 처리
    public void markAsRead() {
        this.readAt = LocalDateTime.now();
    }

    // 알림 생성 정적 팩토리
    public static Notification createFollowNotification(User receiver, User follower) {
        return Notification.builder()
                .receiver(receiver)
                .actor(follower)
                .type(NotificationType.FOLLOW)
                .message(follower.getNickName() + "님이 회원님을 팔로우하기 시작했습니다.")
                .relatedType("FOLLOW")
                .redirectUrl("/profile/" + follower.getId())
                .build();
    }

    public static Notification createGroupCreateNotification(User receiver, User creator,
            GroupV2 group) {
        return Notification.builder()
                .receiver(receiver)
                .actor(creator)
                .type(NotificationType.GROUP_CREATE)
                .message(creator.getNickName() + "님이 새 모임을 생성하셨습니다.")
                .relatedId(group.getId())
                .relatedType("GROUP")
                .redirectUrl("/groups/" + group.getId())
                .build();
    }

    public static Notification createGroupJoinRequestNotification(
            User receiver,
            User actor,
            GroupV2 group
    ) {
        return Notification.builder()
                .receiver(receiver) // host
                .actor(actor) // follower
                .type(NotificationType.GROUP_JOIN_REQUEST)
                .message(actor.getNickName() + "님이 \"" + group.getTitle() + "\" 모임에 참여를 신청했어요.")
                .relatedId(group.getId())
                .relatedType("GROUP")
                .redirectUrl("/groups/" + group.getId() + "/attend")
                .build();
    }

    public static Notification createGroupJoinApprovedNotification(User receiver, User actor,
            GroupV2 group) {
        return Notification.builder()
                .receiver(receiver) // host
                .actor(actor) // joiner approve
                .type(NotificationType.GROUP_JOIN_APPROVED)
                .message(actor.getNickName() + "님이 모임 참여 신청을 수락하셨습니다.")
                .relatedId(group.getId())
                .relatedType("GROUP")
                .redirectUrl("/groups/" + group.getId())
                .build();
    }

    public static Notification createGroupJoinRejectedNotification(User receiver, User actor,
            GroupV2 group) {
        return Notification.builder()
                .receiver(receiver) // host
                .actor(actor) // joiner reject
                .type(NotificationType.GROUP_JOIN_REJECTED)
                .message(actor.getNickName() + "님이 모임 참여 신청을 거절하셨습니다.")
                .relatedId(group.getId())
                .relatedType("GROUP")
                .redirectUrl("/groups/" + group.getId())
                .build();
    }

    public static Notification createGroupJoinNotification(User receiver, User actor, GroupV2 group) {
        return Notification.builder()
                .receiver(receiver) // host
                .actor(actor) // joiner
                .type(NotificationType.GROUP_JOIN)
                .message(actor.getNickName() + "님이 \"" + group.getTitle() + "\" 모임에 참여했어요.")
                .relatedId(group.getId())
                .relatedType("GROUP")
                .redirectUrl("/groups/" + group.getId())
                .build();
    }

    public static Notification createGroupLeaveNotification(User receiver, User actor, GroupV2 group) {
        return Notification.builder()
                .receiver(receiver) // host
                .actor(actor) // leaver
                .type(NotificationType.GROUP_LEAVE)
                .message(actor.getNickName() + "님이 \"" + group.getTitle() + "\" 모임을 탈퇴했어요.")
                .relatedId(group.getId())
                .relatedType("GROUP")
                .redirectUrl("/groups/" + group.getId())
                .build();
    }

    public static Notification createGroupDeleteNotification(
            User receiver, User actor, Long groupId, String groupTitle
    ) {
        return Notification.builder()
                .receiver(receiver)
                .actor(actor)
                .type(NotificationType.GROUP_DELETE)
                .message(actor.getNickName() + "님이 \"" + groupTitle + "\" 모임을 삭제했어요.")
                .relatedId(groupId)
                .relatedType("GROUP")
                .redirectUrl("/groups")
                .build();
    }

    public static Notification createGroupJoinKickedNotification(User receiver, User actor, GroupV2 group) {
        return Notification.builder()
                .receiver(receiver) // kicked user
                .actor(actor) // host
                .type(NotificationType.GROUP_JOIN_KICKED)
                .message(actor.getNickName() + "님이 \"" + group.getTitle() + "\" 모임에서 회원님을 강퇴했어요.")
                .relatedId(group.getId())
                .relatedType("GROUP")
                .redirectUrl("/groups") // 더 이상 접근 불가면 리스트가 안전
                .build();
    }
}
