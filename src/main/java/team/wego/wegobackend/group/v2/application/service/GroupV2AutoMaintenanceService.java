package team.wego.wegobackend.group.v2.application.service;

import java.time.LocalDateTime;
import java.util.List;
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

    // 시작 시간에 도달하면 그룹을 자동으로 완료 상태로 이동
    @Transactional
    @Scheduled(cron = "0 */1 * * * *", zone = "Asia/Seoul") // 1분마다
    public void autoFinishByStartTime() {
        LocalDateTime now = LocalDateTime.now();
        int updated = groupV2Repository.bulkFinishByStartTime(now, FINISH_TARGETS);

        if (updated > 0) {
            log.info("[모임 자동종료] 시작시간 도달로 FINISHED 상태 변경 완료. 변경건수={}", updated);
        }
    }

    // 완료된 그룹은 시작 시간으로부터 24시간이 지난 후 영구 삭제
    @Scheduled(cron = "30 */5 * * * *", zone = "Asia/Seoul") // 5분 간격, 30초 간격
    // @Scheduled(cron = "30 */1 * * * *", zone = "Asia/Seoul") // 1분 간격, 30초 오프셋
    public void autoHardDeleteFinishedAfter24h() {
        LocalDateTime threshold = LocalDateTime.now().minusHours(24);
        // LocalDateTime threshold = LocalDateTime.now().minusMinutes(1);

        while (true) {
            List<Long> groupIds = groupV2Repository.findFinishedExpiredGroupIdsByStartTime(
                    threshold, PageRequest.of(0, DELETE_BATCH_SIZE)
            );

            if (groupIds.isEmpty()) {
                return;
            }

            log.info("[모임 자동삭제] 삭제 대상 조회 완료. 대상건수={} 기준시각={}",
                    groupIds.size(), threshold);

            for (Long groupId : groupIds) {
                try {
                    autoDeleteWorker.deleteOneGroupAfter24h(groupId);
                } catch (Exception e) {
                    log.error("[모임 자동삭제] 삭제 처리 실패. groupId={} 원인={}",
                            groupId, e.toString(), e);
                }
            }

            log.info("[모임 자동삭제] 배치 처리 완료. 처리대상건수={}", groupIds.size());
        }
    }
}

