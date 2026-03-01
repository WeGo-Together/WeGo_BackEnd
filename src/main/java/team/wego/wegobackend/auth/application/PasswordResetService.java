package team.wego.wegobackend.auth.application;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.wego.wegobackend.auth.exception.InvalidResetTokenException;
import team.wego.wegobackend.auth.infrastructure.redis.PasswordResetRedisRepository;
import team.wego.wegobackend.user.domain.User;
import team.wego.wegobackend.user.repository.UserRepository;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PasswordResetService {

    @Value("${password-reset.frontend-url}")
    private String frontendUrl;

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final PasswordResetRedisRepository redisRepository;
    private final PasswordResetEmailService emailService;

    /**
     * 비밀번호 재설정 요청.
     * 미가입 이메일이어도 200 OK를 반환하여 이메일 열거 공격을 방지합니다.
     */
    public void requestPasswordReset(String email) {
        boolean exists = userRepository.existsByEmail(email);
        if (!exists) {
            log.debug("비밀번호 재설정 요청: 미가입 이메일 -> {}", email);
            return;
        }

        String token = UUID.randomUUID().toString();
        redisRepository.save(email, token);

        String resetUrl = frontendUrl + "/password-reset?validationValue=" + token;
        emailService.sendPasswordResetEmail(email, resetUrl);

        log.info("비밀번호 재설정 토큰 발급 -> email={}", email);
    }

    /**
     * 검증값 유효성 검사. 토큰을 소비하지 않습니다. (프론트 2단계 검증 1단계)
     */
    public void verifyToken(String token) {
        redisRepository.findEmailByToken(token)
                .orElseThrow(InvalidResetTokenException::new);
    }

    /**
     * 비밀번호 변경. 토큰 유효성 재검사 후 비밀번호 변경 및 토큰·세션 폐기.
     */
    @Transactional
    public void resetPassword(String token, String newPassword) {
        String email = redisRepository.findEmailByToken(token)
                .orElseThrow(InvalidResetTokenException::new);

        User user = userRepository.findByEmail(email)
                .orElseThrow(InvalidResetTokenException::new);

        user.updatePassword(passwordEncoder.encode(newPassword));
        user.updateCurrentSessionid(null);

        redisRepository.deleteByToken(token);

        log.info("비밀번호 재설정 완료 -> email={}", email);
    }
}