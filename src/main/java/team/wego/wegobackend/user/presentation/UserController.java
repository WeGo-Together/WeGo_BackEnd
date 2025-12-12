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
import team.wego.wegobackend.user.application.dto.response.AvailabilityResponse;
import team.wego.wegobackend.user.application.dto.response.UserInfoResponse;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController implements UserControllerDocs {

    private final UserService userService;
    private final FollowService followService;

    /**
     * 프로필 조회
     * */
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserInfoResponse>> profile(@PathVariable Long userId) {

        UserInfoResponse response = userService.getProfile(userId);

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(ApiResponse.success(200,
                response));
    }

    /**
     * 프로필 이미지 변경
     * */
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

    /**
     * 프로필 정보 변경
     * */
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

    /**
     * 알림 설정 변경
     * */
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

    /**
     * 팔로우 요청
     * */
    @PostMapping("/follow")
    public ResponseEntity<ApiResponse<String>> follow(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @RequestParam("followNickname") String followNickname
    ) {

        followService.follow(followNickname, userDetails.getId());

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(201, "팔로우 성공"));
    }

    /**
     * 팔로우 취소
     * */
    @DeleteMapping("/unfollow")
    public ResponseEntity<ApiResponse<String>> unFollow(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @RequestParam("unFollowNickname") String unFollowNickname
    ) {

        followService.unFollow(unFollowNickname, userDetails.getId());

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(ApiResponse.success(200, "팔로우 취소 성공"));
    }

    /**
     * 이메일 중복검사
     * */
    @GetMapping("/email/availability")
    public ResponseEntity<ApiResponse<AvailabilityResponse>> checkEmailAvailability(
        @RequestParam("email") String email
    ) {

        AvailabilityResponse response = userService.checkEmailAvailability(email);

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(ApiResponse.success(200, response));
    }

    /**
     * 닉네임 중복검사
     * */
    @GetMapping("/nickname/availability")
    public ResponseEntity<ApiResponse<AvailabilityResponse>> checkNicknameAvailability(
        @RequestParam("nickname") String nickname
    ) {

        AvailabilityResponse response = userService.checkNicknameAvailability(nickname);

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(ApiResponse.success(200, response));
    }
}
