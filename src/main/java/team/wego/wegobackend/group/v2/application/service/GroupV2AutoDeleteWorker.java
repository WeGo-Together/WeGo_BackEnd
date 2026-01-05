package team.wego.wegobackend.group.v2.application.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import team.wego.wegobackend.group.v2.application.event.GroupDeletedEvent;
import team.wego.wegobackend.group.v2.domain.entity.GroupUserV2Status;
import team.wego.wegobackend.group.v2.domain.entity.GroupV2;
import team.wego.wegobackend.group.v2.domain.entity.GroupV2Status;
import team.wego.wegobackend.group.v2.domain.repository.GroupImageV2Repository;
import team.wego.wegobackend.group.v2.domain.repository.GroupTagV2Repository;
import team.wego.wegobackend.group.v2.domain.repository.GroupUserV2Repository;
import team.wego.wegobackend.group.v2.domain.repository.GroupV2Repository;
import team.wego.wegobackend.image.application.service.ImageUploadService;

@Slf4j
@RequiredArgsConstructor
@Service
public class GroupV2AutoDeleteWorker {

    private final GroupV2Repository groupV2Repository;
    private final GroupUserV2Repository groupUserV2Repository;
    private final GroupTagV2Repository groupTagV2Repository;
    private final GroupImageV2Repository groupImageV2Repository;

    private final ImageUploadService imageUploadService;
    private final ApplicationEventPublisher eventPublisher;

//    //  REQUIRES_NEW를 제대로 적용하려면 다른 빈(프록시)에서 호출
//    @Transactional(propagation = Propagation.REQUIRES_NEW)
//    public void deleteOneGroupAfter24h(Long groupId) {
//        GroupV2 group = groupV2Repository.findById(groupId).orElse(null);
//        if (group == null) {
//            log.warn("[모임 자동삭제] 대상 모임을 찾을 수 없어 건너뜁니다. groupId={}", groupId);
//            return;
//        }
//
//        if (group.getDeletedAt() != null || group.getStatus() != GroupV2Status.FINISHED) {
//            log.info("[모임 자동삭제] 현재 삭제 조건이 아니어서 건너뜁니다. groupId={} status={}",
//                    groupId, group.getStatus());
//            return;
//        }
//
//        // 삭제 전 알림을 위한 정보를 캡처
//        final Long hostId = group.getHost().getId();
//        final String hostNickName = group.getHost().getNickName();
//        final String groupTitle = group.getTitle();
//
//        List<Long> attendeeIds = groupUserV2Repository.findUserIdsByGroupIdAndStatus(
//                groupId, GroupUserV2Status.ATTEND
//        ).stream().filter(id -> !id.equals(hostId)).toList();
//
//        // 삭제 전에 S3 삭제 대상을 포착
//        List<String> variantUrls = groupImageV2Repository.findAllVariantUrlsByGroupId(groupId);
//
//        log.info("[모임 자동삭제] 삭제 시작. groupId={} hostId={} 제목='{}' 참여자수={} S3파일수={}",
//                groupId, hostId, groupTitle, attendeeIds.size(),
//                (variantUrls == null ? 0 : variantUrls.size()));
//
//        // DB delete (same order as deleteHard)
//        groupUserV2Repository.deleteByGroupId(groupId);
//        groupTagV2Repository.deleteByGroupId(groupId);
//        groupImageV2Repository.deleteVariantsByGroupId(groupId);
//        groupImageV2Repository.deleteImagesByGroupId(groupId);
//        groupV2Repository.delete(group);
//
//        registerAfterCommitS3Deletion(groupId, variantUrls);
//        registerAfterCommitGroupDeletedEvent(groupId, hostId, hostNickName, groupTitle,
//                attendeeIds);
//
//        log.info("[모임 자동삭제] DB 삭제 완료 및 커밋 후 작업 등록 완료. groupId={}", groupId);
//    }

    private void registerAfterCommitS3Deletion(Long groupId, List<String> variantUrls) {
        if (variantUrls == null || variantUrls.isEmpty()) {
            log.info("[모임 자동삭제][S3] 삭제할 이미지가 없습니다. groupId={}", groupId);
            return;
        }

        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            log.warn("[모임 자동삭제][S3] 트랜잭션 동기화가 없어 즉시 삭제합니다. groupId={} url개수={}",
                    groupId, variantUrls.size());
            imageUploadService.deleteAllByUrls(variantUrls);
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                try {
                    log.info("[모임 자동삭제][S3] 커밋 완료. S3 이미지 삭제 시작. groupId={} url개수={}",
                            groupId, variantUrls.size());
                    imageUploadService.deleteAllByUrls(variantUrls);
                    log.info("[모임 자동삭제][S3] S3 이미지 삭제 완료. groupId={}", groupId);
                } catch (Exception e) {
                    log.error("[모임 자동삭제][S3] S3 삭제 실패. groupId={} 원인={}",
                            groupId, e.toString(), e);
                }
            }
        });
    }

    private void registerAfterCommitGroupDeletedEvent(
            Long groupId,
            Long hostId,
            String hostNickName,
            String groupTitle,
            List<Long> attendeeIds
    ) {
        if (attendeeIds == null || attendeeIds.isEmpty()) {
            log.info("[모임 자동삭제][알림] 알림 대상자가 없어 발행을 생략합니다. groupId={}", groupId);
            return;
        }

        GroupDeletedEvent event = new GroupDeletedEvent(
                groupId, hostId, hostNickName, groupTitle, attendeeIds
        );

        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            log.warn("[모임 자동삭제][알림] 트랜잭션 동기화가 없어 즉시 발행합니다. groupId={} 대상자수={}",
                    groupId, attendeeIds.size());
            eventPublisher.publishEvent(event);
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                log.info("[모임 자동삭제][알림] 커밋 완료. 삭제 알림 이벤트 발행. groupId={} hostId={} 대상자수={}",
                        groupId, hostId, attendeeIds.size());
                eventPublisher.publishEvent(event);
            }
        });
    }
}
