package team.wego.wegobackend.chat.domain.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import team.wego.wegobackend.common.entity.BaseTimeEntity;
import team.wego.wegobackend.group.v2.domain.entity.GroupV2;
import team.wego.wegobackend.user.domain.User;

@Entity
@Table(name = "chat_room")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoom extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_room_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "chat_type", nullable = false, length = 10)
    private ChatType chatType;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private GroupV2 group;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatParticipant> participants = new ArrayList<>();

    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatMessage> messages = new ArrayList<>();

    @Builder
    private ChatRoom(ChatType chatType, GroupV2 group, LocalDateTime expiresAt) {
        this.chatType = chatType;
        this.group = group;
        this.expiresAt = expiresAt;
    }

    public static ChatRoom createGroupChat(GroupV2 group) {
        return ChatRoom.builder()
                .chatType(ChatType.GROUP)
                .group(group)
                .build();
    }

    public static ChatRoom createDmChat() {
        return ChatRoom.builder()
                .chatType(ChatType.DM)
                .build();
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isGroupChat() {
        return chatType == ChatType.GROUP;
    }

    public boolean isDmChat() {
        return chatType == ChatType.DM;
    }

    public boolean isHost(User user) {
        if (!isGroupChat() || group == null) {
            return false;
        }
        return group.getHost().getId().equals(user.getId());
    }

    public Long getHostId() {
        if (!isGroupChat() || group == null) {
            return null;
        }
        return group.getHost().getId();
    }

    public void addParticipant(ChatParticipant participant) {
        this.participants.add(participant);
        participant.assignToChatRoom(this);
    }

    public void addMessage(ChatMessage message) {
        this.messages.add(message);
        message.assignToChatRoom(this);
    }

}
