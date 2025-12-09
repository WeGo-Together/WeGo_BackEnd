package team.wego.wegobackend.group.application.service;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import team.wego.wegobackend.group.application.dto.response.GroupImageItemResponse;
import team.wego.wegobackend.group.application.dto.response.PreUploadGroupImageItemResponse;
import team.wego.wegobackend.group.application.dto.response.PreUploadGroupImageResponse;
import team.wego.wegobackend.group.domain.entity.Group;
import team.wego.wegobackend.group.domain.entity.GroupImage;
import team.wego.wegobackend.group.domain.exception.GroupErrorCode;
import team.wego.wegobackend.group.domain.exception.GroupException;
import team.wego.wegobackend.group.domain.repository.GroupImageRepository;
import team.wego.wegobackend.image.application.service.ImageUploadService;
import team.wego.wegobackend.image.domain.ImageFile;

@RequiredArgsConstructor
@Service
public class GroupImageService {

    // 인덱스 0 → 440x240, 인덱스 1 → 100x100
    private static final List<Integer> GROUP_WIDTHS = List.of(440, 100);
    private static final List<Integer> GROUP_HEIGHTS = List.of(240, 100);

    private final GroupImageRepository groupImageRepository;
    private final ImageUploadService imageUploadService;

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
            return; // TODO: 이미지가 필수가 아니라면 null/빈 리스트는 그대로 허용 -> 정책 확인 필요
        }

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
}

