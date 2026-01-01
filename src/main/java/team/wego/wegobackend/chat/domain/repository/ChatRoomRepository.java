package team.wego.wegobackend.chat.domain.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import team.wego.wegobackend.chat.domain.entity.ChatRoom;
import team.wego.wegobackend.chat.domain.entity.ChatType;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    Optional<ChatRoom> findByGroupId(Long groupId);

    @Query("SELECT cr FROM ChatRoom cr WHERE cr.chatType = :chatType AND cr.expiresAt < :now")
    List<ChatRoom> findExpiredChatRooms(
            @Param("chatType") ChatType chatType,
            @Param("now") LocalDateTime now
    );

    @Query("""
            SELECT cr FROM ChatRoom cr
            JOIN cr.participants cp
            WHERE cp.user.id = :userId AND cp.status = 'ACTIVE'
            ORDER BY cr.updatedAt DESC
            """)
    List<ChatRoom> findAllByUserIdAndActiveStatus(@Param("userId") Long userId);

    @Query("""
            SELECT cr FROM ChatRoom cr
            JOIN cr.participants cp1
            JOIN cr.participants cp2
            WHERE cr.chatType = 'DM'
            AND cp1.user.id = :userId1 AND cp1.status = 'ACTIVE'
            AND cp2.user.id = :userId2 AND cp2.status = 'ACTIVE'
            """)
    Optional<ChatRoom> findDmChatRoom(
            @Param("userId1") Long userId1,
            @Param("userId2") Long userId2
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from ChatRoom cr where cr.group.id in :groupIds")
    void deleteByGroupIds(@Param("groupIds") List<Long> groupIds);
}
