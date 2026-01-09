package team.wego.wegobackend.group.v2.application.service;

import java.time.Instant;
import java.time.Duration;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import team.wego.wegobackend.group.v2.application.dto.common.PreUploadedGroupImage;
import team.wego.wegobackend.group.v2.infrastructure.redis.PreUploadedGroupImageRedisRepository;
import team.wego.wegobackend.image.application.service.ImageUploadService;

@Slf4j
@RequiredArgsConstructor
@Component
public class PreUploadedGroupImageOrphanGcWorker {

    private final PreUploadedGroupImageRedisRepository redisRepository;
    private final ImageUploadService imageUploadService;

    // “사용자가 업로드 후 모임 생성까지 걸릴 수 있는 최대 시간”
    // 최소 변경이 목적이면 1~2시간 정도를 권장 (너무 짧으면 정상 플로우도 삭제 위험)
//    private static final Duration ORPHAN_GRACE = Duration.ofHours(2);
    private static final Duration ORPHAN_GRACE = Duration.ofSeconds(30);

    // 한 번에 너무 많이 지우지 않도록 제한
    private static final int BATCH_LIMIT = 200;

    // 10분마다 정도면 충분히 안정적
    @Scheduled(fixedDelay = 30_000L)
    public void gc() {
        long thresholdEpochSec = Instant.now().minus(ORPHAN_GRACE).getEpochSecond();

        List<String> candidates = redisRepository.findExpiredCandidates(thresholdEpochSec, BATCH_LIMIT);
        if (candidates.isEmpty()) {
            return;
        }

        int deleted = 0;
        int indexOnly = 0;
        int failed = 0;

        for (String imageKey : candidates) {
            try {
                // 메타가 있어야 S3 URL을 알 수 있음
                PreUploadedGroupImage meta = redisRepository.find(imageKey).orElse(null);

                if (meta == null) {
                    // TTL로 메타는 이미 만료됨 -> 인덱스만 청소
                    redisRepository.removeIndex(imageKey);
                    indexOnly++;
                    continue;
                }

                // S3 삭제 (main + thumb)
                imageUploadService.deleteAllByUrls(List.of(meta.url440x240(), meta.url100x100()));

                // Redis에서 원자적으로 소비(삭제) + index 제거
                // (이미 누가 consume했을 수도 있으니, 여기서는 "있으면 지운다" 수준으로 충분)
                redisRepository.consume(imageKey);

                deleted++;

            } catch (Exception e) {
                failed++;
                // 실패한 건 다음 스케줄에서 재시도하게 “인덱스를 남겨둔다”
                log.error("[PRE_UPLOADED_IMG_GC] failed imageKey={} reason={}",
                        imageKey, e.toString(), e);
            }
        }

        log.info("[PRE_UPLOADED_IMG_GC] done candidates={} deleted={} indexOnly={} failed={}",
                candidates.size(), deleted, indexOnly, failed);
    }
}