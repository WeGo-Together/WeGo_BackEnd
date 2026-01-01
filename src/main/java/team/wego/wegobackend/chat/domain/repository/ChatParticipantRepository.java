package team.wego.wegobackend.chat.domain.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import team.wego.wegobackend.chat.domain.entity.ChatParticipant;
import team.wego.wegobackend.chat.domain.entity.ParticipantStatus;

public interface ChatParticipantRepository extends JpaRepository<ChatParticipant, Long> {

    Optional<ChatParticipant> findByChatRoomIdAndUserId(Long chatRoomId, Long userId);

    List<ChatParticipant> findAllByChatRoomIdAndStatus(Long chatRoomId, ParticipantStatus status);

    @Query("""
            SELECT cp FROM ChatParticipant cp
            WHERE cp.chatRoom.id = :chatRoomId AND cp.status = 'ACTIVE'
            """)
    List<ChatParticipant> findActiveParticipants(@Param("chatRoomId") Long chatRoomId);

    @Query("""
            SELECT COUNT(cp) FROM ChatParticipant cp
            WHERE cp.chatRoom.id = :chatRoomId AND cp.status = 'ACTIVE'
            """)
    int countActiveParticipants(@Param("chatRoomId") Long chatRoomId);

    boolean existsByChatRoomIdAndUserIdAndStatus(Long chatRoomId, Long userId, ParticipantStatus status);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from ChatParticipant cp where cp.chatRoom.group.id in :groupIds")
    void deleteByGroupIds(@Param("groupIds") List<Long> groupIds);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from ChatParticipant cp where cp.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}
