package team.wego.wegobackend.auth.presentation;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import team.wego.wegobackend.auth.application.AuthService;
import team.wego.wegobackend.auth.application.PasswordResetService;
import team.wego.wegobackend.auth.application.dto.request.GoogleLoginRequest;
import team.wego.wegobackend.auth.application.dto.request.LoginRequest;
import team.wego.wegobackend.auth.application.dto.request.PasswordResetConfirmRequest;
import team.wego.wegobackend.auth.application.dto.request.PasswordResetRequest;
import team.wego.wegobackend.auth.application.dto.request.SignupRequest;
import team.wego.wegobackend.auth.application.dto.response.LoginResponse;
import team.wego.wegobackend.auth.application.dto.response.RefreshResponse;
import team.wego.wegobackend.auth.application.dto.response.SignupResponse;
import team.wego.wegobackend.common.response.ApiResponse;
import team.wego.wegobackend.common.security.CustomUserDetails;
import team.wego.wegobackend.common.security.jwt.JwtTokenProvider;
import team.wego.wegobackend.auth.exception.NotFoundRefreshTokenException;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController implements AuthControllerDocs {

    private final AuthService authService;

    private final PasswordResetService passwordResetService;

    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 회원가입
     */
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<SignupResponse>> signup(
        @Valid @RequestBody SignupRequest request) {
        SignupResponse response = authService.signup(request);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(
                201,
                true,
                response));
    }

    /**
     * 서비스 자체 로그인
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
        @Valid @RequestBody LoginRequest request,
        HttpServletResponse response) {
        LoginResponse loginResponse = authService.login(request);

        response.addCookie(createRefreshTokenCookie(loginResponse.getRefreshToken()));

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(ApiResponse.success(
                200,
                true,
                loginResponse
            ));
    }

    /**
     * OAuth-Google 로그인
     */
    @PostMapping("/google")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
        @Valid @RequestBody GoogleLoginRequest request,
        HttpServletResponse response
    ) {

        log.info("google login controlloer Request -> {}", request);
        LoginResponse loginResponse = authService.googleLogin(request);

        response.addCookie(createRefreshTokenCookie(loginResponse.getRefreshToken()));

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(ApiResponse.success(
                200,
                true,
                loginResponse
            ));
    }

    /**
     * 로그아웃
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        HttpServletResponse response) {

        if (userDetails != null) {
            authService.logout(userDetails.getId());
        }
        deleteRefreshTokenCookie(response);

        return ResponseEntity
            .status(HttpStatus.NO_CONTENT)
            .body(ApiResponse.success(
                204,
                true
            ));
    }

    /**
     * 회원탈퇴
     */
    @DeleteMapping("/withdraw")
    public ResponseEntity<ApiResponse<String>> withDraw(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        HttpServletResponse response
    ) {

        authService.withDraw(userDetails.getId());
        deleteRefreshTokenCookie(response);

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(ApiResponse.success(200, "회원탈퇴 성공"));
    }

    /**
     * Access Token 재발급 (Refresh Token Rotation 적용)
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<RefreshResponse>> refresh(
        @CookieValue(name = "refreshToken", required = false) String refreshToken,
        HttpServletResponse response) {
        if (refreshToken == null) {
            throw new NotFoundRefreshTokenException();
        }

        RefreshResponse refreshResponse = authService.refresh(refreshToken);
        response.addCookie(createRefreshTokenCookie(refreshResponse.getRefreshToken()));

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(
                201,
                true,
                refreshResponse
            ));
    }

    /**
     * 비밀번호 재설정 요청 — 이메일 발송
     * 미가입 이메일이어도 200 OK 반환 (이메일 열거 공격 방지)
     */
    @PostMapping("/password-reset/request")
    public ResponseEntity<ApiResponse<Void>> requestPasswordReset(
        @Valid @RequestBody PasswordResetRequest request) {

        passwordResetService.requestPasswordReset(request.email());

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(ApiResponse.success(200, true));
    }

    /**
     * 검증값(validationValue) 유효성 검사
     */
    @GetMapping("/reset-verify")
    public ResponseEntity<ApiResponse<Void>> verifyResetToken(
        @RequestParam String validationValue) {

        passwordResetService.verifyToken(validationValue);

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(ApiResponse.success(200, true));
    }

    /**
     * 비밀번호 변경
     */
    @PostMapping("/password-reset")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
        @Valid @RequestBody PasswordResetConfirmRequest request) {

        passwordResetService.resetPassword(request.token(), request.newPassword());

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(ApiResponse.success(200, true));
    }

    /**
     * Refresh Token HttpOnly 쿠키 생성
     */
    private Cookie createRefreshTokenCookie(String refreshToken) {
        Cookie cookie = new Cookie("refreshToken", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setDomain("wego.monster");
        cookie.setMaxAge((int) jwtTokenProvider.getRefreshTokenExpiration());
        cookie.setAttribute("SameSite", "Strict");
        return cookie;
    }

    /**
     * Refresh Token HttpOnly 쿠키 제거
     */
    private void deleteRefreshTokenCookie(HttpServletResponse response) {
        Cookie deleteCookie = new Cookie("refreshToken", null);
        deleteCookie.setPath("/");
        deleteCookie.setMaxAge(0);
        deleteCookie.setHttpOnly(true);
        deleteCookie.setSecure(true);
        deleteCookie.setDomain("wego.monster");
        deleteCookie.setAttribute("SameSite", "Strict");
        response.addCookie(deleteCookie);
    }
    //TODO : 개발 토큰 발급 엔드포인트 추가
}
