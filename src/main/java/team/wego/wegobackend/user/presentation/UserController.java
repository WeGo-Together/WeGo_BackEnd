package team.wego.wegobackend.user.presentation;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import team.wego.wegobackend.common.response.ApiResponse;
import team.wego.wegobackend.common.security.CustomUserDetails;
import team.wego.wegobackend.image.application.dto.ImageFileResponse;
import team.wego.wegobackend.user.application.FollowService;
import team.wego.wegobackend.user.application.UserService;
import team.wego.wegobackend.user.application.dto.request.ProfileUpdateRequest;
import team.wego.wegobackend.user.application.dto.response.UserInfoResponse;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController implements UserControllerDocs {

    private final UserService userService;
    private final FollowService followService;

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
        log.debug("프로필 조회 API 호출");
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
            .status(HttpStatus.OK)
            .body(ApiResponse.success(200, response));
    }

    @PatchMapping("/profile")
    public ResponseEntity<ApiResponse<UserInfoResponse>> profileInfo(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @Valid @RequestBody ProfileUpdateRequest request
    ) {

        UserInfoResponse response = userService.updateProfileInfo(userDetails.getId(), request);

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(ApiResponse.success(200, response));
    }

    @PatchMapping("/notification")
    public ResponseEntity<ApiResponse<UserInfoResponse>> changeNotificationConfig(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @RequestParam(required = true) Boolean isNotificationEnabled
    ) {

        UserInfoResponse response = userService.updateNotificationEnabled(userDetails.getId(),
            isNotificationEnabled);

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(ApiResponse.success(200, response));
    }

    @PostMapping("/follow")
    public ResponseEntity<ApiResponse<String>> follow(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @Valid @RequestParam("followNickname") String followNickname
    ) {

        followService.follow(followNickname, userDetails.getId());

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(201, "팔로우 성공"));
    }

    @DeleteMapping("/unfollow")
    public ResponseEntity<ApiResponse<String>> unFollow(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @Valid @RequestParam("unFollowNickname") String unFollowNickname
    ) {
        followService.unFollow(unFollowNickname, userDetails.getId());

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(ApiResponse.success(200, "팔로우 취소 성공"));
    }

    //TODO : 팔로우 목록 조회 API

    //TODO : 팔로워 목록 조회 API
}
