package team.wego.wegobackend.chat.domain.entity;

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
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import team.wego.wegobackend.user.domain.User;

@Entity
@Table(
        name = "chat_participant",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_chat_room_user",
                columnNames = {"chat_room_id", "user_id"}
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "participant_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "join_type", nullable = false, length = 10)
    private JoinType joinType;

    @Column(name = "last_read_message_id")
    private Long lastReadMessageId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 10)
    private ParticipantStatus status;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    private ChatParticipant(ChatRoom chatRoom, User user, JoinType joinType) {
        this.chatRoom = chatRoom;
        this.user = user;
        this.joinType = joinType != null ? joinType : JoinType.AUTO;
        this.joinedAt = LocalDateTime.now();
        this.status = ParticipantStatus.ACTIVE;
        this.updatedAt = LocalDateTime.now();
    }

    public static ChatParticipant create(User user, JoinType joinType) {
        return ChatParticipant.builder()
                .user(user)
                .joinType(joinType)
                .build();
    }

    void assignToChatRoom(ChatRoom chatRoom) {
        this.chatRoom = chatRoom;
    }

    public void updateLastReadMessageId(Long messageId) {
        this.lastReadMessageId = messageId;
        this.updatedAt = LocalDateTime.now();
    }

    public void leave() {
        this.status = ParticipantStatus.LEFT;
        this.updatedAt = LocalDateTime.now();
    }

    public void kick() {
        this.status = ParticipantStatus.KICKED;
        this.updatedAt = LocalDateTime.now();
    }

    public void rejoin(JoinType joinType) {
        this.status = ParticipantStatus.ACTIVE;
        this.joinType = joinType;
        this.joinedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isActive() {
        return status == ParticipantStatus.ACTIVE;
    }

    public boolean isLeft() {
        return status == ParticipantStatus.LEFT;
    }

    public boolean isKicked() {
        return status == ParticipantStatus.KICKED;
    }
}
