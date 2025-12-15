package team.wego.wegobackend.notification.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import team.wego.wegobackend.common.security.CustomUserDetails;
import team.wego.wegobackend.notification.application.SseEmitterService;

@Tag(name = "SSE 엔드포인트", description = "SSE 연결을 위한 엔드포인트")
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final SseEmitterService sseEmitterService;

    // SSE 연결 엔드포인트
    @Operation(summary = "알림 연결 엔드포인트", description = "SSE의 경우 헤더 세팅이 불가능합니다. \n"
        + "accessToken의 경우 QueryParam으로 전송하면 됩니다. (ex : /subscribe?accessToken=tokenvalue.....)")
    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@AuthenticationPrincipal CustomUserDetails user) {
        return sseEmitterService.createEmitter(user.getId());
    }
}
