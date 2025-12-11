package team.wego.wegobackend.auth.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.wego.wegobackend.auth.application.dto.request.LoginRequest;
import team.wego.wegobackend.auth.application.dto.request.SignupRequest;
import team.wego.wegobackend.auth.application.dto.response.LoginResponse;
import team.wego.wegobackend.auth.application.dto.response.RefreshResponse;
import team.wego.wegobackend.auth.application.dto.response.SignupResponse;
import team.wego.wegobackend.auth.exception.DeletedUserException;
import team.wego.wegobackend.auth.exception.InvalidPasswordException;
import team.wego.wegobackend.auth.exception.UserAlreadyExistsException;
import team.wego.wegobackend.auth.exception.UserNotFoundException;
import team.wego.wegobackend.common.exception.AppErrorCode;
import team.wego.wegobackend.common.response.ApiResponse;
import team.wego.wegobackend.common.security.Role;
import team.wego.wegobackend.common.security.exception.ExpiredTokenException;
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
            throw new UserAlreadyExistsException();
        }

        User user = User.builder().email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword())).nickName(request.getNickName())
            .role(Role.ROLE_USER)   //default
            .build();

        userRepository.save(user);

        return SignupResponse.from(user);
    }

    /**
     * 로그인
     */
    public LoginResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(UserNotFoundException::new);

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidPasswordException();
        }

        if (user.getDeleted()) {
            throw new DeletedUserException();
        }

        String accessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getEmail(),
            user.getRole().name());

        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId(), user.getEmail());

        Long expiresIn = jwtTokenProvider.getAccessTokenExpiresIn();

        return LoginResponse.of(user, accessToken, refreshToken, expiresIn);
    }

    /**
     * Access Token 재발급
     */
    public RefreshResponse refresh(String refreshToken) {

        if (!jwtTokenProvider.validateRefreshToken(refreshToken)) {
            throw new ExpiredTokenException();
        }

        String email = jwtTokenProvider.getEmailFromToken(refreshToken);

        User user = userRepository.findByEmail(email)
            .orElseThrow(UserNotFoundException::new);

        if (user.getDeleted()) {
            throw new DeletedUserException();
        }

        String newAccessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getEmail(),
            user.getRole().name());

        Long expiresIn = jwtTokenProvider.getAccessTokenExpiresIn();

        return RefreshResponse.of(newAccessToken, expiresIn);
    }

    /**
     * 회원탈퇴
     */
    @Transactional
    public void withDraw(Long userId) {

        User user = userRepository.findById(userId)
            .orElseThrow(UserNotFoundException::new);

        user.updatedeleted(true);

    }
}