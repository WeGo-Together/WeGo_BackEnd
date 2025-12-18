package team.wego.wegobackend.group.v2.infrastructure.redis;

import java.time.Duration;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import team.wego.wegobackend.group.v2.application.dto.common.PreUploadedGroupImage;

@RequiredArgsConstructor
@Repository
public class PreUploadedGroupImageRedisRepository {

    private static final Duration TTL = Duration.ofHours(2);
    private static final String PREFIX = "group:v2:img:pre:";

    private final RedisTemplate<String, PreUploadedGroupImage> preUploadedGroupImageRedisTemplate;

    private String key(String imageKey) {
        return PREFIX + imageKey;
    }

    public void save(PreUploadedGroupImage value) {
        preUploadedGroupImageRedisTemplate.opsForValue().set(
                key(value.imageKey()),
                value,
                TTL
        );
    }

    public Optional<PreUploadedGroupImage> find(String imageKey) {
        PreUploadedGroupImage value =
                preUploadedGroupImageRedisTemplate.opsForValue().get(key(imageKey));
        return Optional.ofNullable(value);
    }

    public Optional<PreUploadedGroupImage> consume(String imageKey) {
        PreUploadedGroupImage value =
                preUploadedGroupImageRedisTemplate.opsForValue().getAndDelete(key(imageKey));
        return Optional.ofNullable(value);
    }
}



