package team.wego.wegobackend.notification.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import team.wego.wegobackend.common.response.ApiResponse;
import team.wego.wegobackend.common.security.CustomUserDetails;
import team.wego.wegobackend.notification.application.dto.response.NotificationListResponse;

@Tag(name = "알림 API", description = "알림와 관련된 API 리스트 \uD83D\uDC08")
public interface NotificationControllerDocs {

    @Operation(summary = "알림 연결 엔드포인트", description = "SSE의 경우 헤더 세팅이 불가능합니다. \n"
        + "accessToken의 경우 QueryParam으로 전송하면 됩니다. (ex : /subscribe?accessToken=tokenvalue.....)")
    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    SseEmitter subscribe(@AuthenticationPrincipal CustomUserDetails user);

    @Operation(summary = "알림 목록 조회 API", description = "커서 기반의 알림 목록 조회 API입니다.")
    @GetMapping
    ResponseEntity<ApiResponse<NotificationListResponse>> notificationList(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @RequestParam(required = false) Long cursor,
        @RequestParam(defaultValue = "20") @Min(1) @Max(100) Integer size
    );

    @Operation(summary = "미읽음 알림 개수 조회 API", description = "읽지 않은 알림의 개수 조회 API입니다. response.data로 바로 접근하면 됩니다.")
    @GetMapping("/unread-count")
    ResponseEntity<ApiResponse<Long>> unreadNotificationCount(
        @AuthenticationPrincipal CustomUserDetails userDetails
    );

    @Operation(summary = "알림 단건 읽음 처리 API", description = "PathVariable로 넘어온 알림 ID에 대해 읽음 처리를 진행합니다.")
    @PostMapping("/{notificationId}/read")
    ResponseEntity<ApiResponse<Void>> readNotification(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable("notificationId") Long notificationId
    );

    @Operation(summary = "알림 전체 읽음 처리 API", description = "현재 로그인 유저의 알림을 전체 읽음 처리하는 API입니다.")
    @PostMapping("/read-all")
    ResponseEntity<ApiResponse<Void>> readAllNotification(
        @AuthenticationPrincipal CustomUserDetails userDetails
    );

}
