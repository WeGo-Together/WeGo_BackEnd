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
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import team.wego.wegobackend.user.domain.User;

@Entity
@Table(name = "chat_message")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    private User sender;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false, length = 10)
    private MessageType messageType;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Builder
    private ChatMessage(ChatRoom chatRoom, User sender, String content, MessageType messageType) {
        this.chatRoom = chatRoom;
        this.sender = sender;
        this.content = content;
        this.messageType = messageType != null ? messageType : MessageType.TEXT;
        this.createdAt = LocalDateTime.now();
    }

    public static ChatMessage createTextMessage(User sender, String content) {
        return ChatMessage.builder()
                .sender(sender)
                .content(content)
                .messageType(MessageType.TEXT)
                .build();
    }

    public static ChatMessage createSystemMessage(String content) {
        return ChatMessage.builder()
                .sender(null)
                .content(content)
                .messageType(MessageType.SYSTEM)
                .build();
    }

    void assignToChatRoom(ChatRoom chatRoom) {
        this.chatRoom = chatRoom;
    }

    public boolean isSystemMessage() {
        return messageType == MessageType.SYSTEM;
    }

    public boolean isTextMessage() {
        return messageType == MessageType.TEXT;
    }
}
