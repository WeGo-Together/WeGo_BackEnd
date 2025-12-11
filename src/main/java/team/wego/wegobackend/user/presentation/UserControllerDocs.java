package team.wego.wegobackend.user.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import team.wego.wegobackend.common.response.ApiResponse;
import team.wego.wegobackend.common.security.CustomUserDetails;
import team.wego.wegobackend.user.application.dto.request.ProfileUpdateRequest;
import team.wego.wegobackend.user.application.dto.response.UserInfoResponse;

@Tag(name = "유저 API", description = "유저와 관련된 API 리스트 \uD83D\uDC08")
public interface UserControllerDocs {

    @Operation(summary = "테스트 API", description = "서버 로직 테스트를 위한 엔드포인트입니다.")
    ResponseEntity<ApiResponse<String>> test(
        @AuthenticationPrincipal CustomUserDetails userDetails);

    @Operation(summary = "유저 프로필 조회 API", description = "PathVariable로 들어온 userId에 해당하는 유저 프로필에 대한 응답")
    ResponseEntity<ApiResponse<UserInfoResponse>> profile(@PathVariable Long userId);

    @Operation(summary = "프로필 이미지 변경 API", description = "토큰 주인장 이미지만 변경 가능합니다.")
    ResponseEntity<ApiResponse<UserInfoResponse>> profileImage(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @RequestPart("file") MultipartFile file
    );

    @Operation(summary = "유저 프로필 변경 API", description = "토큰 주인장 프로필만 변경 가능합니다.")
    ResponseEntity<ApiResponse<UserInfoResponse>> profileInfo(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @Valid @RequestBody ProfileUpdateRequest request
    );

    @Operation(summary = "알림 설정 변경 API", description = "알림 ON/OFF 플래그 설정")
    ResponseEntity<ApiResponse<UserInfoResponse>> changeNotificationConfig(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @RequestParam(required = true) Boolean isNotificationEnabled
    );

    @Operation(summary = "팔로우 등록 API", description = "요청 ID에 해당하는 사용자를 팔로우합니다.")
    ResponseEntity<ApiResponse<String>> follow(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @Valid @PathVariable("userId") Long userId
    );
}
