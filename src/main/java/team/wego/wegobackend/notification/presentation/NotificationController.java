package team.wego.wegobackend.notification.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import team.wego.wegobackend.auth.exception.UserNotFoundException;
import team.wego.wegobackend.common.response.ApiResponse;
import team.wego.wegobackend.common.security.CustomUserDetails;
import team.wego.wegobackend.notification.application.NotificationService;
import team.wego.wegobackend.notification.application.SseEmitterService;
import team.wego.wegobackend.notification.application.dto.response.NotificationListResponse;
import team.wego.wegobackend.notification.application.dto.response.NotificationResponse;
import team.wego.wegobackend.user.domain.User;
import team.wego.wegobackend.user.repository.UserRepository;

@Tag(name = "SSE 엔드포인트", description = "SSE 연결을 위한 엔드포인트")
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final SseEmitterService sseEmitterService;
    private final NotificationService notificationService;
    private final UserRepository userRepository;    //TEST 의존성 주입

    // SSE 연결 엔드포인트
    @Operation(summary = "알림 연결 엔드포인트", description = "SSE의 경우 헤더 세팅이 불가능합니다. \n"
        + "accessToken의 경우 QueryParam으로 전송하면 됩니다. (ex : /subscribe?accessToken=tokenvalue.....)")
    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@AuthenticationPrincipal CustomUserDetails user) {
        return sseEmitterService.createEmitter(user.getId());
    }

    @GetMapping(value = "/test")
    public ResponseEntity<ApiResponse<String>> test(
        @AuthenticationPrincipal CustomUserDetails user
    ) {

        User testUser = userRepository.findById(user.getId())
            .orElseThrow(UserNotFoundException::new);

        NotificationResponse dto = NotificationResponse.from(testUser);
        sseEmitterService.sendNotification(user.getId(), dto);

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(ApiResponse.success(200, "TEST SUCCESS"));
    }

    /**
     * 알림 목록 조회
     * */
    @GetMapping
    public ResponseEntity<ApiResponse<NotificationListResponse>> notificationList(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @RequestParam(required = false) Long cursor,
        @RequestParam(defaultValue = "20") @Min(1) @Max(100) Integer size
    ) {

        NotificationListResponse response = notificationService.notificationList(userDetails.getId(), cursor, size);

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(ApiResponse.success(200, response));
    }

    /**
     * 읽지 않은 알림 개수 조회
     * */
    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Long>> unreadNotificationCount(
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {

        Long response = notificationService.unreadNotificationCount(userDetails.getId());

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(ApiResponse.success(200, response));
    }

    /**
     * 단건 읽음 처리
     * POST   /notifications/{id}/read
     * POST   /notifications/read-all
     * */
    @PostMapping("/{notificationId}/read")
    public ResponseEntity<ApiResponse<Void>> readNotification(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable("notificationId") Long notificationId
    ) {

        notificationService.readNotification(userDetails.getId(), notificationId);

        return ResponseEntity
            .status(HttpStatus.NO_CONTENT)
            .body(ApiResponse.success(204, null));
    }

    @PostMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> readAllNotification(
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {

        notificationService.readAllNotification(userDetails.getId());

        return ResponseEntity
            .status(HttpStatus.NO_CONTENT)
            .body(ApiResponse.success(204, null));
    }

}
