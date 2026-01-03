package team.wego.wegobackend.chat.application.dto.response;

import java.time.LocalDateTime;
import team.wego.wegobackend.chat.domain.entity.MessageType;

/**
 * 채팅방 추방 시 WebSocket을 통해 전송되는 페이로드
 */
public record ChatKickPayload(
        Long chatRoomId,
        Long targetUserId,
        String targetUserName,
        MessageType messageType,
        LocalDateTime timestamp
) {
    public static ChatKickPayload of(Long chatRoomId, Long targetUserId, String targetUserName) {
        return new ChatKickPayload(
                chatRoomId,
                targetUserId,
                targetUserName,
                MessageType.KICK,
                LocalDateTime.now()
        );
    }
}
