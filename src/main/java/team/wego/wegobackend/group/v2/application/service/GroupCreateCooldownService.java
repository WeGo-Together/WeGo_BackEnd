package team.wego.wegobackend.group.v2.application.service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import team.wego.wegobackend.group.domain.exception.GroupErrorCode;
import team.wego.wegobackend.group.domain.exception.GroupException;

@Service
@RequiredArgsConstructor
public class GroupCreateCooldownService {

    private static final String PREFIX = "group:v2:create:cooldown:";

    private final StringRedisTemplate redisTemplate;

    private String key(Long userId) {
        return PREFIX + userId;
    }

    public void acquireOrThrow(Long userId, int cooldownSeconds) {
        if (userId == null) {
            throw new GroupException(GroupErrorCode.USER_ID_NULL);
        }
        if (cooldownSeconds <= 0) {
            throw new GroupException(GroupErrorCode.INVALID_COOLDOWN_SECONDS, cooldownSeconds);
        }

        String redisKey = key(userId);

        boolean ok = Boolean.TRUE.equals(
                redisTemplate.opsForValue().setIfAbsent(
                        redisKey,
                        "1",
                        Duration.ofSeconds(cooldownSeconds)
                )
        );

        if (!ok) {
            long ttl = redisTemplate.getExpire(redisKey, TimeUnit.SECONDS);

            // Redis TTL 규약:
            // -2: 키 없음, -1: 만료시간 없음
            long remainingSeconds = (ttl >= 0) ? ttl : cooldownSeconds;

            throw new GroupException(
                    GroupErrorCode.GROUP_CREATE_COOLDOWN_ACTIVE,
                    remainingSeconds
            );
        }
    }

    public void release(Long userId) {
        if (userId == null) {
            return;
        }
        redisTemplate.delete(key(userId));
    }

    public void acquireOrThrowWithRollbackRelease(Long userId, int cooldownSeconds) {
        acquireOrThrow(userId, cooldownSeconds);

        // create()는 @Transactional 안에서 호출되니까 보통 true
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronization() {
                        @Override
                        public void afterCompletion(int status) {
                            if (status == STATUS_ROLLED_BACK) {
                                release(userId);
                            }
                        }
                    }
            );
        }
    }
}
