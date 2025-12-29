package team.wego.wegobackend.chat.application.dto.response;

import java.time.LocalDateTime;

/**
 * 추방 알림 페이로드 (WebSocket을 통해 추방된 사용자에게 전송)
 */
public record KickNotificationPayload(
        String type,
        Long chatRoomId,
        String message,
        LocalDateTime timestamp
) {
    public static KickNotificationPayload of(Long chatRoomId) {
        return new KickNotificationPayload(
                "KICKED",
                chatRoomId,
                "채팅방에서 퇴장되었습니다",
                LocalDateTime.now()
        );
    }
}
