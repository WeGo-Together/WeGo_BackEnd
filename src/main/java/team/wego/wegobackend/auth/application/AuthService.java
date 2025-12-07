package team.wego.wegobackend.auth.application;

import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.wego.wegobackend.auth.application.dto.request.LoginRequest;
import team.wego.wegobackend.auth.application.dto.request.SignupRequest;
import team.wego.wegobackend.auth.application.dto.response.LoginResponse;
import team.wego.wegobackend.auth.application.dto.response.RefreshResponse;
import team.wego.wegobackend.auth.application.dto.response.SignupResponse;
import team.wego.wegobackend.common.security.Role;
import team.wego.wegobackend.common.security.jwt.JwtTokenProvider;
import team.wego.wegobackend.user.domain.User;
import team.wego.wegobackend.user.repository.UserRepository;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;

    private final BCryptPasswordEncoder passwordEncoder;

    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 회원가입
     *
     * @return
     */
    @Transactional
    public SignupResponse signup(SignupRequest request) {
        // 이메일 중복 체크
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 존재하는 회원");
        }

        User user = User.builder().email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword())).nickName(request.getNickName())
            .phoneNumber(request.getPhoneNumber()).role(Role.ROLE_USER)   //default
            .build();

        userRepository.save(user);

        return SignupResponse.from(user);
    }

    /**
     * 로그인
     */
    public LoginResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("잘못된 비밀번호");
        }

        if (user.getDeleted()) {
            throw new IllegalArgumentException("탈퇴한 계정");
        }

        String accessToken = jwtTokenProvider.createAccessToken(user.getEmail(),
            user.getRole().name());

        String refreshToken = jwtTokenProvider.createRefreshToken(user.getEmail());

        Long expiresIn = jwtTokenProvider.getAccessTokenExpiresIn();

        return LoginResponse.of(user, accessToken, refreshToken, expiresIn);
    }

    /**
     * Access Token 재발급
     */
    public RefreshResponse refresh(String refreshToken) {

        if (!jwtTokenProvider.validateRefreshToken(refreshToken)) {
            throw new IllegalArgumentException("유효하지 않은 Refresh Token");
        }

        String email = jwtTokenProvider.getEmailFromToken(refreshToken);

        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자"));

        if (user.getDeleted()) {
            throw new IllegalArgumentException("탈퇴한 계정");
        }

        String newAccessToken = jwtTokenProvider.createAccessToken(
            user.getEmail(),
            user.getRole().name()
        );

        Long expiresIn = jwtTokenProvider.getAccessTokenExpiresIn();

        return RefreshResponse.of(newAccessToken, expiresIn);
    }
}