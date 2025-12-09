package team.wego.wegobackend.user.presentation;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import team.wego.wegobackend.common.response.ApiResponse;
import team.wego.wegobackend.common.security.CustomUserDetails;
import team.wego.wegobackend.user.application.UserService;
import team.wego.wegobackend.user.application.dto.response.UserInfoResponse;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user")
public class UserController {

    private final UserService userService;

    @GetMapping("/test")
    public ResponseEntity<ApiResponse<String>> test(
        @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info(LocalDateTime.now() + "test endpoint call, userId -> {}", userDetails.getId());
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(ApiResponse.success(
                200,
                true,
                "Test Success"
            ));
    }

    @GetMapping("{userId}")
    public ResponseEntity<ApiResponse<UserInfoResponse>> profile(@PathVariable Long userId) {
        UserInfoResponse response = userService.getProfile(userId);

        return ResponseEntity
            .status(200)
            .body(ApiResponse.success(200,
                response));
    }
}
