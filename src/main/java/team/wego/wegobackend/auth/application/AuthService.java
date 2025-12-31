package team.wego.wegobackend.auth.application;

import jakarta.annotation.PostConstruct;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import team.wego.wegobackend.auth.application.dto.request.GoogleLoginRequest;
import team.wego.wegobackend.auth.application.dto.request.LoginRequest;
import team.wego.wegobackend.auth.application.dto.request.SignupRequest;
import team.wego.wegobackend.auth.application.dto.response.GoogleTokenResponse;
import team.wego.wegobackend.auth.application.dto.response.GoogleUserInfoResponse;
import team.wego.wegobackend.auth.application.dto.response.LoginResponse;
import team.wego.wegobackend.auth.application.dto.response.RefreshResponse;
import team.wego.wegobackend.auth.application.dto.response.SignupResponse;
import team.wego.wegobackend.auth.entity.UserCounter;
import team.wego.wegobackend.auth.exception.DeletedUserException;
import team.wego.wegobackend.auth.exception.InvalidPasswordException;
import team.wego.wegobackend.auth.exception.NicknameAlreadyExistsException;
import team.wego.wegobackend.auth.exception.NotInitializedUserCounterException;
import team.wego.wegobackend.auth.exception.UserAlreadyExistsException;
import team.wego.wegobackend.auth.exception.UserNotFoundException;
import team.wego.wegobackend.auth.repository.UserCounterRepository;
import team.wego.wegobackend.common.security.Role;
import team.wego.wegobackend.common.security.exception.ExpiredTokenException;
import team.wego.wegobackend.common.security.jwt.JwtTokenProvider;
import team.wego.wegobackend.user.domain.ProviderType;
import team.wego.wegobackend.user.domain.User;
import team.wego.wegobackend.user.repository.UserRepository;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    @Value("${google.oauth.client-id}")
    private String clientId;

    @Value("${google.oauth.client-secret}")
    private String clientSecret;

    private final UserRepository userRepository;

    private final BCryptPasswordEncoder passwordEncoder;

    private final JwtTokenProvider jwtTokenProvider;

    private final RestClient restClient;

    private final UserCounterRepository userCounterRepository;

    /**
     * 회원가입
     */
    @Transactional
    public SignupResponse signup(SignupRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException();
        }

        if (userRepository.existsByNickName(request.getNickName())) {
            throw new NicknameAlreadyExistsException();
        }

        User user = User.builder().email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword())).nickName(request.getNickName())
            .provider(ProviderType.LOCAL)
            .role(Role.ROLE_USER)   //default
            .build();

        userRepository.save(User.createLocalUser(
            request.getEmail(),
            passwordEncoder.encode(request.getPassword()),
            request.getNickName(),
            Role.ROLE_USER));

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
     * Oauth-Google
     */
    @Transactional
    public LoginResponse googleLogin(GoogleLoginRequest request) {

        log.debug("authorizationCode -> {}", request.authorizationCode());
        log.debug("redirectUri -> {}", request.redirectUri());

        String requestBody = String.format(
            "code=%s&client_id=%s&client_secret=%s&redirect_uri=%s&grant_type=authorization_code",
            request.authorizationCode(), clientId, clientSecret, request.redirectUri()
        );

        log.debug("requestBody -> {}", requestBody);

        GoogleTokenResponse response = restClient.post()
            .uri("https://oauth2.googleapis.com/token")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(requestBody)
            .retrieve()
            .body(GoogleTokenResponse.class);

        log.debug("accessToken -> {}", response.getAccessToken());

        GoogleUserInfoResponse userInfo = restClient.get()
            .uri("https://www.googleapis.com/oauth2/v3/userinfo")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + response.getAccessToken())
            .retrieve()
            .body(GoogleUserInfoResponse.class);

        log.debug("사용자 정보 조회 성공 -> {}", userInfo);

        Optional<User> user = userRepository.findByProviderAndProviderId(ProviderType.GOOGLE,
            userInfo.getId());

        if (user.isEmpty()) {
            String nickname = generateAutoNickname();   //닉네임 자동 생성

            log.debug("create Init nickname -> {}", nickname);

            user = Optional.of(userRepository.save(User.createGoogleUser(
                userInfo.getEmail(),
                nickname,
                userInfo.getPicture(),
                userInfo.getId(),
                ProviderType.GOOGLE,
                Role.ROLE_USER
            )));

        }

        User loginUser = user.get();

        String accessToken = jwtTokenProvider.createAccessToken(loginUser.getId(),
            loginUser.getEmail(),
            loginUser.getRole().name());

        String refreshToken = jwtTokenProvider.createRefreshToken(loginUser.getId(),
            loginUser.getEmail());

        Long expiresIn = jwtTokenProvider.getAccessTokenExpiresIn();

        return LoginResponse.of(loginUser, accessToken, refreshToken, expiresIn);
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

    /**
     * OAuth 유저 초기 닉네임 생성 함수 호출측 Transactional 상속
     */
    private String generateAutoNickname() {

        UserCounter counter = userCounterRepository
            .findWithLock()
            .orElseThrow(NotInitializedUserCounterException::new);

        String nickname;
        do {
            counter.plusCounter();
            nickname = "wego_" + counter.getCounter();
        } while (userRepository.existsByNickName(nickname));

        return nickname;
    }

}