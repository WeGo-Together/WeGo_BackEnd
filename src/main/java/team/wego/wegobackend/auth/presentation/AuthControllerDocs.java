package team.wego.wegobackend.auth.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestBody;
import team.wego.wegobackend.auth.application.dto.request.LoginRequest;
import team.wego.wegobackend.auth.application.dto.request.SignupRequest;
import team.wego.wegobackend.auth.application.dto.response.LoginResponse;
import team.wego.wegobackend.auth.application.dto.response.RefreshResponse;
import team.wego.wegobackend.auth.application.dto.response.SignupResponse;
import team.wego.wegobackend.common.response.ApiResponse;
import team.wego.wegobackend.common.security.CustomUserDetails;

@Tag(name = "인증/인가 API", description = "인증 및 인가에 대한 API 리스트 \uD83D\uDC08")
public interface AuthControllerDocs {

    @Operation(summary = "회원가입", description = "회원가입을 위한 엔드포인트")
    ResponseEntity<ApiResponse<SignupResponse>> signup(
        @Valid @RequestBody SignupRequest request);

    @Operation(summary = "로그인", description = "로그인을 위한 엔드포인트")
    ResponseEntity<ApiResponse<LoginResponse>> login(
        @Valid @RequestBody LoginRequest request,
        HttpServletResponse response);

    @Operation(summary = "로그아웃", description = "리프레시 토큰 쿠키만 삭제합니다.")
    ResponseEntity<ApiResponse<Void>> logout(HttpServletResponse response);

    @Operation(summary = "액세스 토큰 재발급", description = "리프레시 토큰 만료가 안되었을 경우 액세스 토큰을 재발급합니다.")
    ResponseEntity<ApiResponse<RefreshResponse>> refresh(
        @CookieValue(name = "refreshToken", required = false) String refreshToken);

    @Operation(summary = "회원탈퇴", description = "DB Soft Delete + refreshCookie 제거")
    ResponseEntity<ApiResponse<String>> withDraw(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        HttpServletResponse response
    );

}
