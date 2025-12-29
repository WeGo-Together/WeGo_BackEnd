package team.wego.wegobackend.chat.domain.repository;

import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import team.wego.wegobackend.chat.domain.entity.ChatMessage;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    @Query("""
            SELECT cm FROM ChatMessage cm
            WHERE cm.chatRoom.id = :chatRoomId AND cm.id < :cursor
            ORDER BY cm.createdAt DESC
            """)
    Slice<ChatMessage> findByChatRoomIdAndIdLessThan(
            @Param("chatRoomId") Long chatRoomId,
            @Param("cursor") Long cursor,
            Pageable pageable
    );

    @Query("""
            SELECT cm FROM ChatMessage cm
            WHERE cm.chatRoom.id = :chatRoomId
            ORDER BY cm.createdAt DESC
            """)
    Slice<ChatMessage> findByChatRoomIdOrderByCreatedAtDesc(
            @Param("chatRoomId") Long chatRoomId,
            Pageable pageable
    );

    @Query("""
            SELECT cm FROM ChatMessage cm
            WHERE cm.chatRoom.id = :chatRoomId
            ORDER BY cm.createdAt DESC
            LIMIT 1
            """)
    Optional<ChatMessage> findLatestByChatRoomId(@Param("chatRoomId") Long chatRoomId);

    @Query("""
            SELECT COUNT(cm) FROM ChatMessage cm
            WHERE cm.chatRoom.id = :chatRoomId AND cm.id > :lastReadMessageId
            """)
    int countUnreadMessages(
            @Param("chatRoomId") Long chatRoomId,
            @Param("lastReadMessageId") Long lastReadMessageId
    );
}
