package team.wego.wegobackend.group.application.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import team.wego.wegobackend.group.application.dto.response.CreateGroupImageResponse;
import team.wego.wegobackend.group.application.dto.response.GroupImageItemResponse;
import team.wego.wegobackend.group.domain.entity.Group;
import team.wego.wegobackend.group.domain.entity.GroupImage;
import team.wego.wegobackend.group.domain.exception.GroupErrorCode;
import team.wego.wegobackend.group.domain.exception.GroupException;
import team.wego.wegobackend.group.domain.repository.GroupImageRepository;
import team.wego.wegobackend.group.domain.repository.GroupRepository;
import team.wego.wegobackend.image.application.service.ImageUploadService;
import team.wego.wegobackend.image.domain.ImageFile;
import team.wego.wegobackend.user.domain.User;
import team.wego.wegobackend.user.repository.UserRepository;

@RequiredArgsConstructor
@Service
public class GroupImageService {

    // 인덱스 0 → 440x240, 인덱스 1 → 100x100
    private static final List<Integer> GROUP_WIDTHS = List.of(440, 100);
    private static final List<Integer> GROUP_HEIGHTS = List.of(240, 100);

    private final GroupImageRepository groupImageRepository;
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final ImageUploadService imageUploadService;

    private void validateCreateGroupImageRequest(List<MultipartFile> images) {
//        // TODO: 이미지가 필수는 아니라고 가정한다면? (필수면 여기서 예외 던지면 됨)
//        if (images == null || images.isEmpty()) {
//            return;
//        }

        if (images.size() > 3) {
            throw new GroupException(GroupErrorCode.IMAGE_UPLOAD_EXCEED, images.size());
        }
    }

    /**
     * 모임 이미지 저장: S3 업로드 + GroupImage 저장 + 응답 DTO 생성
     */
    private List<GroupImageItemResponse> saveGroupImages(Group group, List<MultipartFile> images) {
        if (images == null || images.isEmpty()) {
            return List.of();
        }

        List<GroupImage> entities = new ArrayList<>();
        List<ImageFile> mainFiles = new ArrayList<>();   // 440x240
        List<ImageFile> thumbFiles = new ArrayList<>();  // 100x100
        List<String> uploadedKeys = new ArrayList<>();

        String dir = "groups/" + group.getId();

        try {
            for (int i = 0; i < images.size(); i++) {
                MultipartFile file = images.get(i);
                if (file == null || file.isEmpty()) {
                    continue;
                }

                // 한 장당 2개(440x240, 100x100) 업로드
                List<ImageFile> variants = imageUploadService.uploadAsWebpWithSizes(
                        dir,
                        file,
                        i,
                        GROUP_WIDTHS,
                        GROUP_HEIGHTS
                );

                ImageFile main = variants.get(0);   // 440x240
                ImageFile thumb = variants.get(1);  // 100x100

                // DB에는 대표(440x240) 기준 URL만 저장
                GroupImage image = GroupImage.create(group, main.url(), i);
                entities.add(image);
                mainFiles.add(main);
                thumbFiles.add(thumb);

                // 보상 트랜잭션용
                uploadedKeys.add(main.key());
                uploadedKeys.add(thumb.key());
            }

            if (!entities.isEmpty()) {
                groupImageRepository.saveAll(entities); // 여기서 IDENTITY ID 세팅
            }

            // 엔티티 + 두 사이즈 URL을 묶어서 응답 DTO로 변환
            List<GroupImageItemResponse> responses = new ArrayList<>();
            for (int i = 0; i < entities.size(); i++) {

                GroupImage entity = entities.get(i);
                ImageFile main = mainFiles.get(i);
                ImageFile thumb = thumbFiles.get(i);

                responses.add(
                        GroupImageItemResponse.from(
                                entity,
                                main.url(),   // 440x240
                                thumb.url()   // 100x100
                        )
                );
            }

            return responses;
        } catch (RuntimeException e) {
            imageUploadService.deleteAll(uploadedKeys);
            throw new GroupException(GroupErrorCode.IMAGE_UPLOAD_FAILED, e);
        }
    }

    @Transactional
    public CreateGroupImageResponse createGroupImage(
            Long userId,
            Long groupId,
            List<MultipartFile> images
    ) {
        // 1. 회원 조회(HOST)
        User host = userRepository.findById(userId)
                .orElseThrow(() -> new GroupException(GroupErrorCode.HOST_USER_NOT_FOUND, userId));

        // 2. 모임 조회
        Group group = groupRepository.findById(groupId)
                .orElseThrow(
                        () -> new GroupException(GroupErrorCode.GROUP_NOT_FOUND_BY_ID, groupId));

        // 3. 업로드하는 사람이 host인지 검증
        if (!group.getHost().getId().equals(host.getId())) {
            throw new GroupException(GroupErrorCode.HOST_USER_NOT_FOUND, userId);
        }

        // 4. 비즈니스 유효성 검사
        validateCreateGroupImageRequest(images);

        // 5. 업로드 + DB 저장 + 응답 DTO 생성
        List<GroupImageItemResponse> imageItems = saveGroupImages(group, images);

        // 6. 대표 이미지 = sortOrder가 가장 작은 이미지(0)
        GroupImageItemResponse representativeImage = imageItems.stream()
                .min(Comparator.comparingInt(GroupImageItemResponse::sortOrder))
                .orElse(null);

        return new CreateGroupImageResponse(
                group.getId(),
                representativeImage,
                imageItems
        );
    }
}

