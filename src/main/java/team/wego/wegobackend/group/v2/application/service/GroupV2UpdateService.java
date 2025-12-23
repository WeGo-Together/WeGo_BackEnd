package team.wego.wegobackend.group.v2.application.service;

import jakarta.persistence.EntityManager;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.wego.wegobackend.group.domain.exception.GroupErrorCode;
import team.wego.wegobackend.group.domain.exception.GroupException;
import team.wego.wegobackend.group.v2.application.dto.common.Address;
import team.wego.wegobackend.group.v2.application.dto.common.GroupImageItem;
import team.wego.wegobackend.group.v2.application.dto.common.PreUploadedGroupImage;
import team.wego.wegobackend.group.v2.application.dto.request.CreateGroupImageV2Request;
import team.wego.wegobackend.group.v2.application.dto.request.UpdateGroupV2Request;
import team.wego.wegobackend.group.v2.application.dto.response.UpdateGroupV2Response;
import team.wego.wegobackend.group.v2.domain.entity.GroupImageV2;
import team.wego.wegobackend.group.v2.domain.entity.GroupTagV2;
import team.wego.wegobackend.group.v2.domain.entity.GroupUserV2Status;
import team.wego.wegobackend.group.v2.domain.entity.GroupV2;
import team.wego.wegobackend.group.v2.domain.entity.GroupV2Address;
import team.wego.wegobackend.group.v2.domain.repository.GroupImageV2Repository;
import team.wego.wegobackend.group.v2.domain.repository.GroupUserV2Repository;
import team.wego.wegobackend.group.v2.domain.repository.GroupV2Repository;
import team.wego.wegobackend.group.v2.infrastructure.redis.PreUploadedGroupImageRedisRepository;
import team.wego.wegobackend.tag.application.service.TagService;
import team.wego.wegobackend.tag.domain.entity.Tag;

@RequiredArgsConstructor
@Service
public class GroupV2UpdateService {

    private final GroupV2Repository groupV2Repository;
    private final GroupImageV2Repository groupImageV2Repository;
    private final PreUploadedGroupImageRedisRepository preUploadedGroupImageRedisRepository;
    private final GroupUserV2Repository groupUserV2Repository;

    private final TagService tagService;

    private static final int TEMP_SORT_ORDER = Integer.MAX_VALUE;
    private static final int MAX_IMAGES = 3;

    private final EntityManager em;


    @Transactional
    public UpdateGroupV2Response update(Long userId, Long groupId, UpdateGroupV2Request request) {
        if (userId == null) {
            throw new GroupException(GroupErrorCode.USER_ID_NULL);
        }

        GroupV2 group = groupV2Repository.findById(groupId)
                .orElseThrow(
                        () -> new GroupException(GroupErrorCode.GROUP_NOT_FOUND_BY_ID, groupId));

        // 권한 체크(호스트만
        if (!group.getHost().getId().equals(userId)) {
            throw new GroupException(GroupErrorCode.GROUP_ONLY_HOST_CAN_UPDATE, groupId, userId);
        }

        // 엔티티 단 공통 가드: deleteAT + CANCELLED, FINISHED 체크하자
        group.assertUpdatable();

        // 1 스칼라 필드
        applyScalarUpdates(group, groupId, request);

        // 2 상태 변경(요청이 있을 때만)
        if (request.status() != null) {
            group.changeStatus(request.status());
        }

        // 3 태그 변경(null이면 변경 없음)
        if (request.tags() != null) {
            applyTags(group, request.tags());
        }

        // 4 이미지 변경(null이면 변경 없음)

        if (request.images() != null) {
            applyImagesWithSafeReorder(group, userId, request.images());
        }

        // dirty checking으로 충분. 그래도 명시적으로 save 해도 무방.
        groupV2Repository.save(group);

        // 응답 구성(조회로 안전하게)
        List<String> tagNames = group.getGroupTags().stream()
                .map(gt -> gt.getTag().getName())
                .toList();

        // 이미지는 variants 포함해서 다시 조회
        List<GroupImageV2> images = groupImageV2Repository.findAllByGroupIdWithVariants(groupId);

        // 정렬 + DTO 변환
        List<GroupImageItem> imageItems = images.stream()
                .sorted(Comparator.comparingInt(GroupImageV2::getSortOrder))
                .map(GroupImageItem::from)
                .toList();

        // 이미지가 0개면 기본 이미지(variants 2개) 1장 내려주기
        if (imageItems.isEmpty()) {
            imageItems = new ArrayList<>();
        }

        return new UpdateGroupV2Response(
                group.getId(),
                group.getTitle(),
                group.getJoinPolicy(),
                group.getStatus(),
                Address.from(group.getAddress()),
                group.getStartTime(),
                group.getEndTime(),
                imageItems,
                tagNames,
                group.getDescription(),
                group.getMaxParticipants(),
                group.getUpdatedAt()
        );
    }

    private void applyScalarUpdates(GroupV2 group, Long groupId, UpdateGroupV2Request request) {
        if (request.title() != null) { // 제목
            group.changeTitle(request.title());
        }
        if (request.description() != null) { // 설명
            group.changeDescription(request.description());
        }

        if (request.location() != null || request.locationDetail() != null) { // 주소
            String newLocation = request.location() != null
                    ? request.location()
                    : group.getAddress().getLocation();

            String newDetail = request.locationDetail() != null
                    ? request.locationDetail()
                    : group.getAddress().getLocationDetail();

            group.changeAddress(GroupV2Address.of(newLocation, newDetail));
        }

        // 시간: 둘 중 하나만 와도 엔티티가 최종 검증하도록 설계했으면 각각 호출
        if (request.startTime() != null && request.endTime() != null) {
            group.changeTime(request.startTime(), request.endTime());
        } else {
            if (request.startTime() != null) {
                group.changeStartTime(request.startTime());
            }
            if (request.endTime() != null) {
                group.changeEndTime(request.endTime());
            }
        }

        // 정원 변경: 참석자 수보다 작아지면 409
        if (request.maxParticipants() != null) {
            long attendCount = groupUserV2Repository.countByGroupIdAndStatus(groupId,
                    GroupUserV2Status.ATTEND);
            if (request.maxParticipants() < attendCount) {
                throw new GroupException(GroupErrorCode.MAX_PARTICIPANTS_BELOW_ATTEND_COUNT,
                        attendCount);
            }
            group.changeMaxParticipants(request.maxParticipants());
        }
    }

    private void applyTags(GroupV2 group, List<String> tagNames) {
        List<String> cleaned = tagNames.stream()
                .filter(t -> t != null && !t.isBlank())
                .map(String::trim)
                .toList();

        if (cleaned.size() > 10) {
            throw new GroupException(GroupErrorCode.TAG_EXCEED_MAX, cleaned.size());
        }

        // "중복이면 에러" 정책이라면:
        if (new LinkedHashSet<>(cleaned).size() != cleaned.size()) {
            throw new GroupException(GroupErrorCode.TAG_DUPLICATED);
        }

        List<Tag> tags = tagService.findOrCreateAll(cleaned);

        // 교체
        new ArrayList<>(group.getGroupTags()).forEach(group::removeTag);
        em.flush();
        for (Tag tag : tags) {
            GroupTagV2.create(group, tag);
        }
    }

    /**
     * applyImagesWithSafeReorder()가 “DTO → 최종 desiredKeys”를 만들어주는 역할 이미지 업데이트: 요청 imageKeys를 "최종
     * 상태(순서)"로 해석한다. - 요청 순서대로 sortOrder=0.. 부여 (0번이 대표) - 요청에 없는 기존 이미지는 삭제(orphanRemoval) - 요청에
     * 있고 기존에 없으면 preupload(REDIS) consume 후 새로 생성(insert) - (group_id, sort_order) 유니크를 유지하기 위해
     * 2-phase 임시 sortOrder 사용
     * <p>
     * 정책: - 최대 3장 - 중복 key 금지 - [] 허용: 전체 삭제
     */
    private void applyImagesWithSafeReorder(
            GroupV2 group,
            Long userId,
            List<CreateGroupImageV2Request> raw
    ) {
        // 0) raw는 null이 아닌 상태로 들어온다 (update()에서 null 체크)
        // [] => 전체 삭제는 "명시적"으로 raw.isEmpty()일 때만
        if (raw.isEmpty()) {
            new ArrayList<>(group.getImages()).forEach(group::removeImage);
            em.flush();
            return;
        }

        // 1) 아이템 유효성(엄격): null item, null/blank imageKey는 400
        for (CreateGroupImageV2Request it : raw) {
            if (it == null) {
                throw new GroupException(GroupErrorCode.INVALID_GROUP_IMAGE_ITEM, group.getId(),
                        userId);
            }
            if (it.imageKey() == null || it.imageKey().isBlank()) {
                throw new GroupException(GroupErrorCode.INVALID_GROUP_IMAGE_KEY, group.getId(),
                        userId);
            }
            // sortOrder는 nullable 허용 (없으면 아래에서 자동 배정 가능)
            if (it.sortOrder() != null) {
                if (it.sortOrder() < 0) {
                    throw new GroupException(GroupErrorCode.INVALID_GROUP_IMAGE_SORT_ORDER_RANGE,
                            group.getId(), userId);
                }
                // 최대 3장 정책이면 0~2로 제한하는 게 예측 가능
                if (it.sortOrder() >= MAX_IMAGES) {
                    throw new GroupException(GroupErrorCode.INVALID_GROUP_IMAGE_SORT_ORDER_RANGE,
                            group.getId(), userId);
                }
            }
        }

        // 2) trim 적용 (정합성 유지)
        List<CreateGroupImageV2Request> cleaned = raw.stream()
                .map(it -> new CreateGroupImageV2Request(it.imageKey().trim(), it.sortOrder()))
                .toList();

        // 3) 최대 3장
        if (cleaned.size() > MAX_IMAGES) {
            throw new GroupException(GroupErrorCode.IMAGE_UPLOAD_EXCEED, cleaned.size());
        }

        // 4) imageKey 중복 금지
        List<String> keys = cleaned.stream().map(CreateGroupImageV2Request::imageKey).toList();
        if (new LinkedHashSet<>(keys).size() != keys.size()) {
            throw new GroupException(GroupErrorCode.DUPLICATED_IMAGE_KEY_IN_REQUEST);
        }

        // 5) 최종 순서 결정
        boolean allNullSortOrder = cleaned.stream().allMatch(it -> it.sortOrder() == null);

        List<String> desiredKeys;
        if (allNullSortOrder) {
            // sortOrder가 모두 없으면 요청 순서가 곧 최종 순서 (0번이 대표)
            desiredKeys = cleaned.stream()
                    .map(CreateGroupImageV2Request::imageKey)
                    .toList();
        } else {
            // sortOrder 중복 검사
            Set<Integer> used = new HashSet<>();
            for (CreateGroupImageV2Request it : cleaned) {
                if (it.sortOrder() != null) {
                    if (!used.add(it.sortOrder())) {
                        throw new GroupException(
                                GroupErrorCode.DUPLICATED_IMAGE_SORT_ORDER_IN_REQUEST,
                                group.getId(), userId);
                    }
                }
            }

            // null sortOrder 자동 배정 (0..2 중 빈 자리)
            int next = 0;
            List<ItemWithIndex> normalized = new ArrayList<>();
            for (int i = 0; i < cleaned.size(); i++) {
                CreateGroupImageV2Request it = cleaned.get(i);
                Integer so = it.sortOrder();

                if (so == null) {
                    while (used.contains(next)) {
                        next++;
                    }
                    // next도 0..2 보장(최대 3장이라 여기서 넘칠 일은 거의 없지만 방어)
                    if (next >= MAX_IMAGES) {
                        throw new GroupException(
                                GroupErrorCode.INVALID_GROUP_IMAGE_SORT_ORDER_RANGE, group.getId(),
                                userId);
                    }
                    so = next;
                    used.add(so);
                }

                normalized.add(new ItemWithIndex(i, it.imageKey(), so));
            }

            // sortOrder 오름차순, tie면 원래 요청 순서
            desiredKeys = normalized.stream()
                    .sorted(Comparator
                            .comparingInt(ItemWithIndex::sortOrder)
                            .thenComparingInt(ItemWithIndex::index))
                    .map(ItemWithIndex::imageKey)
                    .toList();
        }

        // 6) 최종 상태 리스트(desiredKeys)를 기존 “안전 재정렬 로직”으로 반영
        applyFinalImageKeysWithSafeReorder(group, userId, desiredKeys);
    }

    // applyFinalImageKeysWithSafeReorder()는 “최종 desiredKeys 적용” 역할
    private void applyFinalImageKeysWithSafeReorder(GroupV2 group, Long userId,
            List<String> finalKeys) {
        // 1) 최종 키 정규화(방어적으로 trim)
        List<String> desiredKeys = finalKeys.stream()
                .map(String::trim)
                .toList();

        // 2) 공통 검증 (중복/개수)
        if (desiredKeys.size() > MAX_IMAGES) {
            throw new GroupException(GroupErrorCode.IMAGE_UPLOAD_EXCEED, desiredKeys.size());
        }
        if (new LinkedHashSet<>(desiredKeys).size() != desiredKeys.size()) {
            throw new GroupException(GroupErrorCode.DUPLICATED_IMAGE_KEY_IN_REQUEST);
        }

        // 3) 최종 키가 빈 리스트면 전체 삭제
        if (desiredKeys.isEmpty()) {
            new ArrayList<>(group.getImages()).forEach(group::removeImage);
            em.flush();
            return;
        }

        // 4) 요청에 없는 기존 이미지 삭제
        Set<String> desiredKeySet = new HashSet<>(desiredKeys);
        List<GroupImageV2> toRemove = group.getImages().stream()
                .filter(img -> !desiredKeySet.contains(img.getImageKey()))
                .toList();
        toRemove.forEach(group::removeImage);

        // 5) 남은 이미지 임시 음수 이동 (유니크 충돌 방지)
        List<GroupImageV2> remaining = group.getImages();
        for (int i = 0; i < remaining.size(); i++) {
            remaining.get(i).changeSortOrder(-(i + 1));
        }

        em.flush();

        // 6) 삭제 후 기준으로 존재하는 key map
        Map<String, GroupImageV2> afterRemoveByKey = group.getImages().stream()
                .collect(Collectors.toMap(GroupImageV2::getImageKey, img -> img));

        // 7) 새로 생성해야 하는 키
        List<String> toCreateKeys = desiredKeys.stream()
                .filter(k -> !afterRemoveByKey.containsKey(k))
                .toList();

        int temp = TEMP_SORT_ORDER;
        for (String key : toCreateKeys) {
            PreUploadedGroupImage pre = preUploadedGroupImageRedisRepository.consume(key)
                    .orElseThrow(() -> new GroupException(
                            GroupErrorCode.PRE_UPLOADED_IMAGE_NOT_FOUND, key));

            if (!userId.equals(pre.uploaderId())) {
                throw new GroupException(GroupErrorCode.PRE_UPLOADED_IMAGE_OWNER_MISMATCH, key);
            }

            GroupImageV2.create(group, temp--, pre.imageKey(), pre.url440x240(), pre.url100x100());
        }

        // 8) 최종 매핑 + 검증
        Map<String, GroupImageV2> afterByKey = group.getImages().stream()
                .collect(Collectors.toMap(GroupImageV2::getImageKey, img -> img, (a, b) -> a));

        for (String key : desiredKeys) {
            if (!afterByKey.containsKey(key)) {
                throw new GroupException(
                        GroupErrorCode.GROUP_IMAGE_NOT_FOUND_IN_GROUP_AFTER_UPDATE, key);
            }
        }

        // 9) 최종 sortOrder 부여
        for (int i = 0; i < desiredKeys.size(); i++) {
            afterByKey.get(desiredKeys.get(i)).changeSortOrder(i);
        }
    }

    private record ItemWithIndex(
            int index,
            String imageKey,
            int sortOrder
    ) {

    }

}

