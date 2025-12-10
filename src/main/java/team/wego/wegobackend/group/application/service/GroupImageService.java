package team.wego.wegobackend.group.application.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import team.wego.wegobackend.group.application.dto.request.UpdateGroupImageItemRequest;
import team.wego.wegobackend.group.application.dto.response.GroupImageItemResponse;
import team.wego.wegobackend.group.application.dto.response.PreUploadGroupImageItemResponse;
import team.wego.wegobackend.group.application.dto.response.PreUploadGroupImageResponse;
import team.wego.wegobackend.group.domain.entity.Group;
import team.wego.wegobackend.group.domain.entity.GroupImage;
import team.wego.wegobackend.group.domain.exception.GroupErrorCode;
import team.wego.wegobackend.group.domain.exception.GroupException;
import team.wego.wegobackend.group.domain.repository.GroupImageRepository;
import team.wego.wegobackend.group.domain.repository.GroupRepository;
import team.wego.wegobackend.image.application.service.ImageUploadService;
import team.wego.wegobackend.image.domain.ImageFile;

@RequiredArgsConstructor
@Service
public class GroupImageService {

    // 인덱스 0: 440x240, 인덱스 1: 100x100
    private static final List<Integer> GROUP_WIDTHS = List.of(440, 100);
    private static final List<Integer> GROUP_HEIGHTS = List.of(240, 100);

    private final GroupImageRepository groupImageRepository;
    private final ImageUploadService imageUploadService;
    private final GroupRepository groupRepository;


    public PreUploadGroupImageResponse uploadGroupImages(List<MultipartFile> images) {
        validateCreateGroupImageRequest(images);

        if (images == null || images.isEmpty()) {
            return new PreUploadGroupImageResponse(List.of());
        }

        List<PreUploadGroupImageItemResponse> responses = new ArrayList<>();

        for (int i = 0; i < images.size(); i++) {
            MultipartFile file = images.get(i);
            if (file == null || file.isEmpty()) {
                continue;
            }

            // 한 장당 2개(440x240, 100x100) 업로드
            List<ImageFile> variants = imageUploadService.uploadAsWebpWithSizes(
                    file,
                    i,
                    GROUP_WIDTHS,
                    GROUP_HEIGHTS
            );

            ImageFile main = variants.get(0);   // 440x240
            ImageFile thumb = variants.get(1);  // 100x100

            responses.add(new PreUploadGroupImageItemResponse(i, main.url(), thumb.url()));
        }

        return new PreUploadGroupImageResponse(responses);
    }

    private void validateCreateGroupImageRequest(List<MultipartFile> images) {
        if (images == null || images.isEmpty()) {
            return;
        }

        if (images.size() > 3) {
            throw new GroupException(GroupErrorCode.IMAGE_UPLOAD_EXCEED, images.size());
        }
    }

    private List<GroupImageItemResponse> saveGroupImages(Group group, List<MultipartFile> images) {
        if (images == null || images.isEmpty()) {
            return List.of();
        }

        List<GroupImage> entities = new ArrayList<>();
        List<GroupImageItemResponse> responses = new ArrayList<>();
        List<String> uploadedKeys = new ArrayList<>();

        try {
            for (int i = 0; i < images.size(); i++) {
                MultipartFile file = images.get(i);
                if (file == null || file.isEmpty()) {
                    continue;
                }

                // 한 장당 2개(440x240, 100x100) 업로드
                List<ImageFile> variants = imageUploadService.uploadAsWebpWithSizes(
                        file,
                        i,
                        GROUP_WIDTHS,
                        GROUP_HEIGHTS
                );

                ImageFile main = variants.get(0);   // 440x240
                ImageFile thumb = variants.get(1);  // 100x100

                // DB에는 두 행으로 저장 (같은 sortOrder = i)
                GroupImage mainEntity = GroupImage.create(group, main.url(), i);
                GroupImage thumbEntity = GroupImage.create(group, thumb.url(), i);

                entities.add(mainEntity);
                entities.add(thumbEntity);

                // 응답은 한 세트로
                responses.add(
                        GroupImageItemResponse.from(
                                mainEntity,
                                thumbEntity
                        )
                );

                // 롤백용 key 기록
                uploadedKeys.add(main.key());
                uploadedKeys.add(thumb.key());
            }

            if (!entities.isEmpty()) {
                groupImageRepository.saveAll(entities);
            }

            return responses;
        } catch (RuntimeException e) {
            imageUploadService.deleteAll(uploadedKeys);
            throw new GroupException(GroupErrorCode.IMAGE_UPLOAD_FAILED, e);
        }
    }


    @Transactional
    public List<GroupImageItemResponse> updateGroupImages(
            Long userId,
            Long groupId,
            List<UpdateGroupImageItemRequest> requests
    ) {
        // 1. 그룹 조회 (soft delete 고려)
        Group group = groupRepository.findByIdAndDeletedAtIsNull(groupId)
                .orElseThrow(
                        () -> new GroupException(GroupErrorCode.GROUP_NOT_FOUND_BY_ID, groupId)
                );

        // 2. HOST 권한 체크
        if (!group.getHost().getId().equals(userId)) {
            throw new GroupException(
                    GroupErrorCode.NO_PERMISSION_TO_UPDATE_GROUP,
                    groupId,
                    userId
            );
        }

        // 3. null 방어 및 최대 개수 제한: 논리 이미지 세트 기준
        List<UpdateGroupImageItemRequest> safeList =
                (requests == null) ? List.of() : requests.stream()
                        .filter(Objects::nonNull)
                        .toList();

        if (safeList.size() > 3) {
            throw new GroupException(GroupErrorCode.IMAGE_UPLOAD_EXCEED, safeList.size());
        }

        // 4. 기존 이미지 URL 수집 -> S3 삭제
        List<String> oldUrls = group.getImages().stream()
                .map(GroupImage::getImageUrl)
                .filter(Objects::nonNull)
                .toList();

        imageUploadService.deleteAllByUrls(oldUrls);

        // 5. 기존 GroupImage 엔티티들을 관계에서 제거: DB에서도 삭제됨
        group.getImages().clear();
        groupImageRepository.flush();  // DB 즉시 삭제하기 위해 추가

        // 6. 새 GroupImage 엔티티 생성
        List<GroupImage> newEntities = new ArrayList<>();

        for (int i = 0; i < safeList.size(); i++) {
            UpdateGroupImageItemRequest item = safeList.get(i);

            int sortOrder = (item.sortOrder() != null) ? item.sortOrder() : i;

            String mainUrl = item.imageUrl440x240();
            String thumbUrl = item.imageUrl100x100();

            if (mainUrl != null && !mainUrl.isBlank()) {
                GroupImage main = GroupImage.create(group, mainUrl, sortOrder);
                newEntities.add(main);
            }

            if (thumbUrl != null && !thumbUrl.isBlank()) {
                GroupImage thumb = GroupImage.create(group, thumbUrl, sortOrder);
                newEntities.add(thumb);
            }
        }

        if (!newEntities.isEmpty()) {
            groupImageRepository.saveAll(newEntities);
        }

        // 7. sortOrder 기준으로 한 세트 단위 응답 DTO 구성
        Map<Integer, List<GroupImage>> byOrder = newEntities.stream()
                .collect(Collectors.groupingBy(GroupImage::getSortOrder));

        return byOrder.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> {
                    List<GroupImage> list = entry.getValue();

                    GroupImage main = list.stream()
                            .filter(img -> img.getImageUrl().contains("440x240"))
                            .findFirst()
                            .orElse(list.getFirst());

                    GroupImage thumb = list.stream()
                            .filter(img -> img.getImageUrl().contains("100x100"))
                            .findFirst()
                            .orElse(null);

                    return GroupImageItemResponse.from(main, thumb);

                })
                .toList();
    }


    @Transactional
    public void deleteGroupImages(Long userId, Long groupId) {
        // 1. 그룹 조회
        Group group = groupRepository.findByIdAndDeletedAtIsNull(groupId)
                .orElseThrow(
                        () -> new GroupException(GroupErrorCode.GROUP_NOT_FOUND_BY_ID, groupId)
                );

        // 2. HOST 권한 체크
        if (!group.getHost().getId().equals(userId)) {
            throw new GroupException(
                    GroupErrorCode.NO_PERMISSION_TO_UPDATE_GROUP,
                    groupId,
                    userId
            );
        }

        // 3. 기존 URL들 수집 후 S3 삭제
        List<String> oldUrls = group.getImages().stream()
                .map(GroupImage::getImageUrl)
                .filter(Objects::nonNull)
                .toList();

        imageUploadService.deleteAllByUrls(oldUrls);

        group.getImages().clear();
        groupImageRepository.flush(); // DB 즉시 삭제하기 위해 추가
    }
}
