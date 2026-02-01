package team.wego.wegobackend.group.v2.infrastructure.redis;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;
import team.wego.wegobackend.group.v2.application.dto.common.PreUploadedGroupImage;

@RequiredArgsConstructor
@Repository
public class PreUploadedGroupImageRedisRepository {

    // 메타데이터는 넉넉히: 1일 (GC, 장애 대비)
    private static final Duration META_TTL = Duration.ofDays(1);

    private static final String PREFIX = "group:v2:img:pre:";
    private static final String IDX_KEY = "group:v2:img:pre:idx";

    private final RedisTemplate<String, PreUploadedGroupImage> valueTemplate;
    private final StringRedisTemplate stringRedisTemplate;

    private String key(String imageKey) {
        return PREFIX + imageKey;
    }

    public void save(PreUploadedGroupImage value) {
        valueTemplate.opsForValue().set(key(value.imageKey()), value, META_TTL);

        long score = value.createdAt().atZone(java.time.ZoneId.systemDefault()).toEpochSecond();
        stringRedisTemplate.opsForZSet().add(IDX_KEY, value.imageKey(), score);
        // IDX 자체 TTL은 옵션: 하루에 한 번 갱신해도 되고, 그냥 둬도 됩니다(멤버 정리로 관리).
    }


    // 소비는 원자적으로 가져가면서 삭제
    public Optional<PreUploadedGroupImage> consume(String imageKey) {
        PreUploadedGroupImage value =
                valueTemplate.opsForValue().getAndDelete(key(imageKey));

        // 인덱스에서도 제거(있든 없든)
        stringRedisTemplate.opsForZSet().remove(IDX_KEY, imageKey);

        return Optional.ofNullable(value);
    }

    public Optional<PreUploadedGroupImage> find(String imageKey) {
        return Optional.ofNullable(valueTemplate.opsForValue().get(key(imageKey)));
    }


    // 고아 이미지 삭제 전용: 특정 시간 이전 imageKey들 배치로 뽑기
    public List<String> findExpiredCandidates(long thresholdEpochSec, int limit) {
        // ZRANGEBYSCORE idx -inf threshold LIMIT 0 limit
        Set<String> set = stringRedisTemplate.opsForZSet()
                .rangeByScore(IDX_KEY, Double.NEGATIVE_INFINITY, thresholdEpochSec, 0, limit);

        if (set == null || set.isEmpty()) return List.of();
        return new ArrayList<>(set);
    }

    public void removeIndex(String imageKey) {
        stringRedisTemplate.opsForZSet().remove(IDX_KEY, imageKey);
    }
}

