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
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import team.wego.wegobackend.common.entity.BaseTimeEntity;
import team.wego.wegobackend.notification.application.dto.NotificationType;
import team.wego.wegobackend.user.domain.User;

@Entity
@Table(name = "notification")
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

    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;

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
        this.isRead = false;
        this.relatedId = relatedId;
        this.relatedType = relatedType;
        this.redirectUrl = redirectUrl;
    }

    // 읽음 처리
    public void markAsRead() {
        this.isRead = true;
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

    //TODO : receiver -> 모임장, related 데이터 작성 필요
    public static Notification createEnterNotification(User receiver, User participant, Long postId) {
        return Notification.builder()
            .receiver(receiver)
            .actor(participant)
            .type(NotificationType.ENTER)
            .message(participant.getNickName() + "님이 모임에 참여하셨습니다.")
            .relatedId(postId)
            .relatedType("POST")
            .redirectUrl("/post/" + postId)
            .build();
    }

    //TODO : receiver -> 모임장, related 데이터 작성 필요
    public static Notification createExitNotification(User receiver, User leaver,
        Long postId, Long commentId) {
        return Notification.builder()
            .receiver(receiver)
            .actor(leaver)
            .type(NotificationType.EXIT)
            .message(leaver.getNickName() + "님이 모임에서 퇴장하셨습니다.")
            .relatedId(commentId)
            .relatedType("POST")
            .redirectUrl("/post/" + postId)
            .build();
    }


    public static Notification createGroupNotification(User receiver, User creator,
        Long postId) {
        return Notification.builder()
            .receiver(receiver)
            .actor(creator)
            .type(NotificationType.CREATE)
            .message(creator.getNickName() + "님이 모임을 생성하셨습니다.")
            .relatedId(postId)
            .relatedType("POST")
            .redirectUrl("/post/" + postId)
            .build();
    }

    public static Notification createGroupCancleNotification(User receiver, User canceler,
        Long postId) {
        return Notification.builder()
            .receiver(receiver)
            .actor(canceler)
            .type(NotificationType.CANCLE)
            .message(canceler.getNickName() + "님이 모임을 취소하셨습니다.")
            .relatedId(postId)
            .relatedType("POST")
            .redirectUrl("/post/" + postId)
            .build();
    }

}
