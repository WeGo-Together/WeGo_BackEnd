package team.wego.wegobackend.auth.infrastructure.redis;

import java.time.Duration;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

/**
 * 비밀번호 재설정 토큰 Redis 저장소.
 *
 * <p>이중 키 구조로 "계정당 최신 토큰 1개만 유효" 정책을 구현합니다.
 * <ul>
 *   <li>pwreset:token:{uuid}  → email  (토큰 검증용)</li>
 *   <li>pwreset:email:{email} → uuid   (이전 토큰 추적 및 폐기용)</li>
 * </ul>
 */
@Repository
@RequiredArgsConstructor
public class PasswordResetRedisRepository {

    private static final String TOKEN_PREFIX = "pwreset:token:";
    private static final String EMAIL_PREFIX = "pwreset:email:";

    @Value("${password-reset.token-ttl-minutes:30}")
    private long tokenTtlMinutes;

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 토큰을 저장합니다. 기존 토큰이 있으면 먼저 폐기합니다.
     */
    public void save(String email, String token) {
        // 기존 토큰 폐기
        String oldToken = stringRedisTemplate.opsForValue().get(emailKey(email));
        if (oldToken != null) {
            stringRedisTemplate.delete(tokenKey(oldToken));
        }

        Duration ttl = Duration.ofMinutes(tokenTtlMinutes);
        stringRedisTemplate.opsForValue().set(tokenKey(token), email, ttl);
        stringRedisTemplate.opsForValue().set(emailKey(email), token, ttl);
    }

    /**
     * 토큰으로 이메일을 조회합니다.
     */
    public Optional<String> findEmailByToken(String token) {
        return Optional.ofNullable(stringRedisTemplate.opsForValue().get(tokenKey(token)));
    }

    /**
     * 사용 완료된 토큰을 폐기합니다. (토큰 키, 이메일 역방향 키 모두 삭제)
     */
    public void deleteByToken(String token) {
        String email = stringRedisTemplate.opsForValue().get(tokenKey(token));
        stringRedisTemplate.delete(tokenKey(token));
        if (email != null) {
            stringRedisTemplate.delete(emailKey(email));
        }
    }

    private String tokenKey(String token) {
        return TOKEN_PREFIX + token;
    }

    private String emailKey(String email) {
        return EMAIL_PREFIX + email;
    }
}