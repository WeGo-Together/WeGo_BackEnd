package team.wego.wegobackend.group.v2.application.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import team.wego.wegobackend.group.domain.exception.GroupErrorCode;
import team.wego.wegobackend.group.domain.exception.GroupException;
import team.wego.wegobackend.group.v2.application.event.GroupDeletedEvent;
import team.wego.wegobackend.group.v2.domain.entity.GroupUserV2Status;
import team.wego.wegobackend.group.v2.domain.entity.GroupV2;
import team.wego.wegobackend.group.v2.domain.repository.GroupImageV2Repository;
import team.wego.wegobackend.group.v2.domain.repository.GroupTagV2Repository;
import team.wego.wegobackend.group.v2.domain.repository.GroupUserV2Repository;
import team.wego.wegobackend.group.v2.domain.repository.GroupV2Repository;
import team.wego.wegobackend.image.application.service.ImageUploadService;

@Slf4j
@RequiredArgsConstructor
@Service
public class GroupV2DeleteService {

    private final GroupV2Repository groupV2Repository;
    private final GroupUserV2Repository groupUserV2Repository;
    private final GroupTagV2Repository groupTagV2Repository;
    private final GroupImageV2Repository groupImageV2Repository;

    private final ImageUploadService imageUploadService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void deleteHard(Long userId, Long groupId) {
        if (userId == null) {
            throw new GroupException(GroupErrorCode.USER_ID_NULL);
        }

        GroupV2 group = groupV2Repository.findById(groupId)
                .orElseThrow(
                        () -> new GroupException(GroupErrorCode.GROUP_NOT_FOUND_BY_ID, groupId));

        if (!group.getHost().getId().equals(userId)) {
            throw new GroupException(GroupErrorCode.GROUP_ONLY_HOST_CAN_UPDATE, groupId, userId);
        }

        // 삭제 알림에 필요한 정보는 삭제 전에 확보
        final String hostNickName = group.getHost().getNickName();
        final String groupTitle = group.getTitle();

        List<Long> attendeeIds = groupUserV2Repository.findUserIdsByGroupIdAndStatus(
                groupId, GroupUserV2Status.ATTEND
        ).stream().filter(id -> !id.equals(userId)).toList();

        log.info("[GROUP_DELETE] start groupId={} hostId={} title='{}' attendeeCount={}",
                groupId, userId, groupTitle, attendeeIds.size());

        // S3 삭제 대상 URL도 삭제 전에 확보
        List<String> variantUrls = groupImageV2Repository.findAllVariantUrlsByGroupId(groupId);

        // DB 삭제
        groupUserV2Repository.deleteByGroupId(groupId);
        groupTagV2Repository.deleteByGroupId(groupId);
        groupImageV2Repository.deleteVariantsByGroupId(groupId);
        groupImageV2Repository.deleteImagesByGroupId(groupId);
        groupV2Repository.delete(group);

        // AFTER_COMMIT 작업 등록
        registerAfterCommitS3Deletion(groupId, variantUrls);
        registerAfterCommitGroupDeletedEvent(groupId, userId, hostNickName, groupTitle,
                attendeeIds);

        log.info(
                "[GROUP_DELETE] registered afterCommit hooks groupId={} s3Urls={} attendeeCount={}",
                groupId, (variantUrls == null ? 0 : variantUrls.size()), attendeeIds.size());
    }

    private void registerAfterCommitS3Deletion(Long groupId, List<String> variantUrls) {
        if (variantUrls == null || variantUrls.isEmpty()) {
            log.info("[GROUP_DELETE][S3] no variant urls. groupId={}", groupId);
            return;
        }

        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            log.warn("[GROUP_DELETE][S3] no tx sync. delete immediately. groupId={} urls={}",
                    groupId, variantUrls.size());
            imageUploadService.deleteAllByUrls(variantUrls);
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                try {
                    log.info(
                            "[GROUP_DELETE][S3][AFTER_COMMIT] deleting s3 objects groupId={} urls={}",
                            groupId, variantUrls.size());
                    imageUploadService.deleteAllByUrls(variantUrls);
                    log.info("[GROUP_DELETE][S3][AFTER_COMMIT] deleted s3 objects groupId={}",
                            groupId);
                } catch (Exception e) {
                    // DB는 이미 커밋됨 → 실패 로그는 반드시 남겨서 추후 재처리(outbox) 근거로
                    log.error("[GROUP_DELETE][S3][AFTER_COMMIT] delete failed groupId={} reason={}",
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
            log.info("[GROUP_DELETE][EVENT] no attendees. skip publish. groupId={}", groupId);
            return;
        }

        GroupDeletedEvent event = new GroupDeletedEvent(
                groupId,
                hostId,
                hostNickName,
                groupTitle,
                attendeeIds
        );

        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            log.warn(
                    "[GROUP_DELETE][EVENT] no tx sync. publish immediately. groupId={} attendeeCount={}",
                    groupId, attendeeIds.size());
            eventPublisher.publishEvent(event);
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                log.info(
                        "[GROUP_DELETE][EVENT][AFTER_COMMIT] publish groupDeleted groupId={} hostId={} attendeeCount={}",
                        groupId, hostId, attendeeIds.size());
                eventPublisher.publishEvent(event);
            }
        });
    }
}
