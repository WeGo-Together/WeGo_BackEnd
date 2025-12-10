package team.wego.wegobackend.user.presentation;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import team.wego.wegobackend.common.response.ApiResponse;
import team.wego.wegobackend.common.security.CustomUserDetails;
import team.wego.wegobackend.image.application.dto.ImageFileResponse;
import team.wego.wegobackend.user.application.UserService;
import team.wego.wegobackend.user.application.dto.request.ProfileUpdateRequest;
import team.wego.wegobackend.user.application.dto.response.UserInfoResponse;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
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

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserInfoResponse>> profile(@PathVariable Long userId) {
        UserInfoResponse response = userService.getProfile(userId);

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(ApiResponse.success(200,
                response));
    }

    @PatchMapping("/profile-image")
    public ResponseEntity<ApiResponse<UserInfoResponse>> profileImage(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @RequestPart("file") MultipartFile file
    ) {

        UserInfoResponse response = userService.updateProfileImage(userDetails.getId(), file);

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(201, response));
    }

    @PatchMapping("/profile")
    public ResponseEntity<ApiResponse<UserInfoResponse>> profileInfo(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @Valid @RequestBody ProfileUpdateRequest request
    ) {

        UserInfoResponse response = userService.updateProfileInfo(userDetails.getId(), request);

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(201, response));
    }

    @PatchMapping("/notification")
    public ResponseEntity<ApiResponse<UserInfoResponse>> changeNotificationConfig(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @RequestParam(required = true) Boolean isNotificationEnabled
    ) {

        UserInfoResponse response = userService.updateNotificationEnabled(userDetails.getId(),
            isNotificationEnabled);

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(201, response));
    }

}
