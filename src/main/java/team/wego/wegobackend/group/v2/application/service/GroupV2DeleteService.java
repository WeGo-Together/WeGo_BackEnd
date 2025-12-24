package team.wego.wegobackend.group.v2.application.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
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

@RequiredArgsConstructor
@Service
public class GroupV2DeleteService {

    private final GroupV2Repository groupV2Repository;
    private final GroupUserV2Repository groupUserV2Repository;
    private final GroupTagV2Repository groupTagV2Repository;
    private final GroupImageV2Repository groupImageV2Repository;

    private final ImageUploadService imageUploadService;

    // SSE 이벤트 호출
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void deleteHard(Long userId, Long groupId) {
        if (userId == null) {
            throw new GroupException(GroupErrorCode.USER_ID_NULL);
        }

        GroupV2 group = groupV2Repository.findById(groupId)
                .orElseThrow(
                        () -> new GroupException(GroupErrorCode.GROUP_NOT_FOUND_BY_ID, groupId));

        // 호스트만 삭제 가능
        if (!group.getHost().getId().equals(userId)) {
            throw new GroupException(GroupErrorCode.GROUP_ONLY_HOST_CAN_UPDATE, groupId, userId);
            // (에러코드가 update용이라면 delete 전용 에러코드 추가를 추천)
        }

        // 1) S3 삭제 대상 URL 확보 (variants의 url 2개씩)
        List<String> variantUrls = groupImageV2Repository.findAllVariantUrlsByGroupId(groupId);

        // 2) DB 삭제 (연관관계가 복잡하므로 "명시적 순서"로 지움)
        // - users 먼저
        groupUserV2Repository.deleteByGroupId(groupId);

        // - group_tags (Tag 자체는 삭제하면 안됨)
        groupTagV2Repository.deleteByGroupId(groupId);

        // - image variants -> images
        groupImageV2Repository.deleteVariantsByGroupId(groupId);
        groupImageV2Repository.deleteImagesByGroupId(groupId);

        // - 마지막으로 group
        groupV2Repository.delete(group);

        // 3) 커밋 이후 S3 삭제 (DB가 실제로 삭제 확정된 다음 파일 삭제)
        registerAfterCommitS3Deletion(variantUrls);

        List<Long> attendeeIds = groupUserV2Repository.findUserIdsByGroupIdAndStatus(
                groupId, GroupUserV2Status.ATTEND
        );

        registerAfterCommitGroupDeletedEvent(groupId, userId, attendeeIds);
    }

    private void registerAfterCommitS3Deletion(List<String> variantUrls) {
        if (variantUrls == null || variantUrls.isEmpty()) {
            return;
        }
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            // 트랜잭션 밖에서 호출되는 이상 케이스 방어: 즉시 삭제
            imageUploadService.deleteAllByUrls(variantUrls);
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                // 여기서 S3 삭제 실패가 나면 DB는 이미 지워짐.
                // 추후 "삭제 재시도(outbox)" 확장 포인트가 필요하면 여기서 기록/로그 남기기.
                imageUploadService.deleteAllByUrls(variantUrls);
            }
        });
    }

    private void registerAfterCommitGroupDeletedEvent(Long groupId, Long hostId,
            List<Long> attendeeIds) {
        if (attendeeIds == null || attendeeIds.isEmpty()) {
            return;
        }
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            eventPublisher.publishEvent(new GroupDeletedEvent(groupId, hostId, attendeeIds));
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                eventPublisher.publishEvent(new GroupDeletedEvent(groupId, hostId, attendeeIds));
            }
        });
    }
}