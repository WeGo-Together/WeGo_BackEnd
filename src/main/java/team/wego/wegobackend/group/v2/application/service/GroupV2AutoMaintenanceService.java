package team.wego.wegobackend.group.v2.application.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.wego.wegobackend.group.v2.domain.entity.GroupV2Status;
import team.wego.wegobackend.group.v2.domain.repository.GroupV2Repository;

@Slf4j
@RequiredArgsConstructor
@Service
public class GroupV2AutoMaintenanceService {

    private final GroupV2Repository groupV2Repository;
    private final GroupV2AutoDeleteWorker autoDeleteWorker;

    private static final List<GroupV2Status> FINISH_TARGETS =
            List.of(GroupV2Status.RECRUITING, GroupV2Status.FULL, GroupV2Status.CLOSED);

    private static final int DELETE_BATCH_SIZE = 200;

    // 동시 실행 방지 가드
    private final AtomicBoolean deleteJobRunning = new AtomicBoolean(false);


    // 시작 시간에 도달하면 그룹을 자동으로 완료 상태로 이동
    @Transactional
    @Scheduled(cron = "0 */1 * * * *", zone = "Asia/Seoul")
    public void autoFinishByStartTime() {
        LocalDateTime now = LocalDateTime.now();
        int updated = groupV2Repository.bulkFinishByStartTime(now, FINISH_TARGETS);

        if (updated > 0) {
            log.info("[모임 자동종료] 시작시간 도달로 FINISHED 상태 변경 완료. 변경건수={}", updated);
        }
    }

    // 완료된 그룹은 시작 시간으로부터 24시간이 지난 후 영구 삭제
    @Scheduled(cron = "0 */1 * * * *", zone = "Asia/Seoul")
    public void autoHardDeleteFinishedAfter24h() {

        // 이미 실행 중이면 이번 트리거는 스킵
        if (!deleteJobRunning.compareAndSet(false, true)) {
            log.warn("[모임 자동삭제] 이미 실행 중이라 이번 스케줄 트리거는 건너뜁니다.");
            return;
        }

        long startedAt = System.currentTimeMillis();
        int totalSuccess = 0;

        try {
            LocalDateTime threshold = LocalDateTime.now().minusHours(24);

            while (true) {
                List<Long> groupIds = groupV2Repository.findFinishedExpiredGroupIdsByStartTime(
                        threshold, PageRequest.of(0, DELETE_BATCH_SIZE)
                );

                if (groupIds.isEmpty()) {
                    return;
                }

                int success = 0;
                for (Long groupId : groupIds) {
                    try {
                        autoDeleteWorker.deleteOneGroupAfter24h(groupId);
                        success++;
                    } catch (Exception e) {
                        log.error("[모임 자동삭제] 삭제 처리 실패. groupId={} 원인={}", groupId, e.toString(), e);
                    }
                }

                totalSuccess += success;

                // 실패 ID 하나로 영원히 루프 점검
                if (success == 0) {
                    log.error("[모임 자동삭제] 배치 내 성공 0건. 무한 루프 방지를 위해 종료합니다. threshold={} batch={}",
                            threshold, groupIds);
                    return;
                }
            }
        } finally {
            deleteJobRunning.set(false);
            log.info("[모임 자동삭제] 작업 종료. totalSuccess={} elapsedMs={}",
                    totalSuccess, (System.currentTimeMillis() - startedAt));
        }
    }
}
