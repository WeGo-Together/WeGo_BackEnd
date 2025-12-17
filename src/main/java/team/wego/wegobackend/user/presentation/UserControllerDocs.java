package team.wego.wegobackend.user.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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
import team.wego.wegobackend.user.application.dto.response.AvailabilityResponse;
import team.wego.wegobackend.user.application.dto.response.FollowListResponse;
import team.wego.wegobackend.user.application.dto.response.UserInfoResponse;

@Tag(name = "유저 API", description = "유저와 관련된 API 리스트 \uD83D\uDC08")
public interface UserControllerDocs {

    @Operation(summary = "본인 프로필 조회 API", description = "토큰을 통해서 본인 프로필 정보를 조회합니다. (별도 파라메터 없습니다)")
    ResponseEntity<ApiResponse<UserInfoResponse>> profile(
        @AuthenticationPrincipal CustomUserDetails userDetails
    );

    @Operation(summary = "유저 프로필 조회 API", description =
        "PathVariable로 들어온 userId에 해당하는 유저 프로필에 대한 응답 \n"
            + "로그인 여부/본인 여부에 따라 팔로우 여부를 null 혹은 true/false로 응답합니다.")
    ResponseEntity<ApiResponse<UserInfoResponse>> profile(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long userId
    );

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

    @Operation(summary = "팔로우 등록 API", description = "요청 닉네임에 해당하는 사용자를 팔로우합니다.")
    ResponseEntity<ApiResponse<String>> follow(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @Valid @RequestParam("followNickname") String followNickname
    );

    @Operation(summary = "팔로우 취소 API", description = "요청 닉네임에 해당하는 사용자를 팔로우 취소합니다.")
    ResponseEntity<ApiResponse<String>> unFollow(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @Valid @RequestParam("unFollowNickname") String unFollowNickname
    );

    @Operation(summary = "팔로우 리스트 조회 API", description = "userId에 해당하는 유저의 팔로우 리스트를 조회합니다.")
    ResponseEntity<ApiResponse<FollowListResponse>> followList(
        @Parameter(description = "조회할 유저 ID") @PathVariable Long userId,
        @Parameter(description = "페이지네이션 커서 (첫 페이지는 null)") @RequestParam(required = false) Long cursor,
        @Parameter(description = "조회 개수 (1-100)") @RequestParam(defaultValue = "20") @Min(1) @Max(100) Integer size
    );

    @Operation(summary = "이메일 중복 검사 API", description = "이메일 중복검사에 대한 응답은 bool값입니다.")
    ResponseEntity<ApiResponse<AvailabilityResponse>> checkEmailAvailability(
        @RequestParam("email") String email
    );

    @Operation(summary = "닉네임 중복 검사 API", description = "닉네임 중복검사에 대한 응답은 bool값입니다.")
    ResponseEntity<ApiResponse<AvailabilityResponse>> checkNicknameAvailability(
        @RequestParam("nickname") String nickname
    );
}
