package team.wego.wegobackend.group.v2.application.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import team.wego.wegobackend.group.domain.exception.GroupErrorCode;
import team.wego.wegobackend.group.domain.exception.GroupException;
import team.wego.wegobackend.group.v2.application.dto.common.PreUploadGroupImageV2Item;
import team.wego.wegobackend.group.v2.application.dto.common.PreUploadedGroupImage;
import team.wego.wegobackend.group.v2.application.dto.response.PreUploadGroupImageV2Response;
import team.wego.wegobackend.group.v2.infrastructure.redis.PreUploadedGroupImageRedisRepository;
import team.wego.wegobackend.image.application.service.ImageUploadService;
import team.wego.wegobackend.image.domain.ImageFile;
import team.wego.wegobackend.image.domain.ImageSize;


@RequiredArgsConstructor
@Service
public class GroupImageV2PreUploadService {

    private final ImageUploadService imageUploadService;
    private final PreUploadedGroupImageRedisRepository redisRepository;

    private static final ImageSize SIZE_MAIN = new ImageSize(440, 240);
    private static final ImageSize SIZE_THUMB = new ImageSize(100, 100);

    public PreUploadGroupImageV2Response uploadGroupImagesV2(Long uploaderId,
            List<MultipartFile> images) {
        validateMax3(images);

        List<PreUploadGroupImageV2Item> result = new ArrayList<>();

        for (int i = 0; i < images.size(); i++) {
            MultipartFile file = images.get(i);
            if (file == null || file.isEmpty()) {
                continue;
            }

            String imageKey = UUID.randomUUID().toString();
            String baseName =
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                            + "_" + i + "_" + imageKey;

            ImageFile main = imageUploadService.uploadAsWebpWithSizeUsingBaseName(file, baseName,
                    SIZE_MAIN);
            ImageFile thumb = imageUploadService.uploadAsWebpWithSizeUsingBaseName(file, baseName,
                    SIZE_THUMB);

            // Redis 저장 (서버가 url 세트를 보증)
            redisRepository.save(new PreUploadedGroupImage(
                    imageKey,
                    uploaderId,
                    main.url(),
                    thumb.url(),
                    LocalDateTime.now()
            ));

            result.add(new PreUploadGroupImageV2Item(imageKey, i, main.url(), thumb.url()));
        }

        return new PreUploadGroupImageV2Response(result);
    }

    private void validateMax3(List<MultipartFile> images) {
        if (images == null || images.isEmpty()) {
            return;
        }
        if (images.size() > 3) {
            throw new GroupException(GroupErrorCode.IMAGE_UPLOAD_EXCEED, images.size());
        }
    }
}
