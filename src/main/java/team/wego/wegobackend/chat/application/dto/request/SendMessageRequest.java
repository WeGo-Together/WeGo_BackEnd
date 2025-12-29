package team.wego.wegobackend.chat.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SendMessageRequest(
        @NotNull(message = "채팅방 ID는 필수입니다")
        Long chatRoomId,

        @NotBlank(message = "메시지 내용은 필수입니다")
        String content
) {
}
