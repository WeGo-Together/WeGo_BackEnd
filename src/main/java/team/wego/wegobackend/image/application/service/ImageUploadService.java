package team.wego.wegobackend.image.application.service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.imageio.ImageIO;
import lombok.RequiredArgsConstructor;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import team.wego.wegobackend.image.config.AwsS3Properties;
import team.wego.wegobackend.image.config.ImageProperties;
import team.wego.wegobackend.image.domain.ImageFile;
import team.wego.wegobackend.image.domain.ImageSize;
import team.wego.wegobackend.image.domain.exception.ImageException;
import team.wego.wegobackend.image.domain.exception.ImageExceptionCode;

@RequiredArgsConstructor
@Service
public class ImageUploadService {

    private static final List<String> ALLOWED_CONTENT_TYPES = List.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    private static final List<String> ALLOWED_EXTENSIONS = List.of(
            ".jpg",
            ".jpeg",
            ".png",
            ".webp"
    );

    private final S3Client s3Client;
    private final AwsS3Properties awsS3Properties;
    private final ImageProperties imageProperties;

    public ImageFile uploadOriginal(String dir, MultipartFile file, int index) {
        validateDir(dir);
        validateImageSize(file);
        validateImageContentType(file);
        validateExtension(file.getOriginalFilename());

        String originalFilename = file.getOriginalFilename();
//        String key = buildKey(dir, originalFilename, index);
        String key = buildKey(originalFilename, index);

        byte[] bytes = resizeIfNeededKeepFormat(file);

        putToS3(key, bytes, file.getContentType());
        String url = awsS3Properties.getPublicEndpoint() + "/" + key;

        return new ImageFile(key, url);
    }

    public List<ImageFile> uploadAllOriginal(String dir, List<MultipartFile> files) {
        List<ImageFile> result = new ArrayList<>();
        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);
            result.add(uploadOriginal(dir, file, i));
        }
        return result;
    }

    public ImageFile uploadAsWebpWithSize(
            String dir,
            MultipartFile file,
            int index,
            ImageSize size
    ) {
        validateDir(dir);
        validateImageSize(file);
        validateImageContentType(file);
        validateExtension(file.getOriginalFilename());

        String baseName = buildBaseName(index);
        String key = dir + "/" + baseName + "_" + size.width() + "x" + size.height() + ".webp";

        byte[] bytes = convertToWebpWithSize(file, size);

        putToS3(key, bytes, "image/webp");
        String url = awsS3Properties.getPublicEndpoint() + "/" + key;

        return new ImageFile(key, url);
    }

    public List<ImageFile> uploadAsWebpWithSizes(
            String dir,
            MultipartFile file,
            int index,
            List<Integer> widths,
            List<Integer> heights
    ) {
        if (widths.size() != heights.size()) {
            // 해당 예외는 이미지 예외 처리로 수행하지 않는다.
            throw new IllegalArgumentException("widths와 heights의 길이가 일치해야 합니다.");
        }

        validateDir(dir);
        validateImageSize(file);
        validateImageContentType(file);
        validateExtension(file.getOriginalFilename());

        String baseName = buildBaseName(index);
        List<ImageFile> result = new ArrayList<>();

        for (int i = 0; i < widths.size(); i++) {
            ImageSize size = new ImageSize(widths.get(i), heights.get(i));

            String key = dir + "/" + baseName + "_" + size.width() + "x" + size.height() + ".webp";
            byte[] bytes = convertToWebpWithSize(file, size);

            putToS3(key, bytes, "image/webp");
            String url = awsS3Properties.getPublicEndpoint() + "/" + key;

            result.add(new ImageFile(key, url));
        }

        return result;
    }

    public List<ImageFile> uploadAsWebpWithSizes(
            MultipartFile file,
            int index,
            List<Integer> widths,
            List<Integer> heights
    ) {
        if (widths.size() != heights.size()) {
            // 해당 예외는 이미지 예외 처리로 수행하지 않는다.
            throw new IllegalArgumentException("widths와 heights의 길이가 일치해야 합니다.");
        }

        validateImageSize(file);
        validateImageContentType(file);
        validateExtension(file.getOriginalFilename());

        String baseName = buildBaseName(index);
        List<ImageFile> result = new ArrayList<>();

        for (int i = 0; i < widths.size(); i++) {
            ImageSize size = new ImageSize(widths.get(i), heights.get(i));

            String key = baseName + "_" + size.width() + "x" + size.height() + ".webp";
            byte[] bytes = convertToWebpWithSize(file, size);

            putToS3(key, bytes, "image/webp");
            String url = awsS3Properties.getPublicEndpoint() + "/" + key;

            result.add(new ImageFile(key, url));
        }

        return result;
    }

    public void delete(String key) {
        s3Client.deleteObject(builder -> builder
                .bucket(awsS3Properties.getBucket())
                .key(key)
        );
    }

    public void deleteAll(List<String> keys) {
        for (String key : keys) {
            delete(key);
        }
    }

    private byte[] convertToWebpWithSize(MultipartFile file, ImageSize size) {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            Thumbnails.of(file.getInputStream())
                    .size(size.width(), size.height())
                    .outputFormat("webp")
                    .toOutputStream(byteArrayOutputStream);

            if (byteArrayOutputStream.size() == 0) {
                throw new ImageException(ImageExceptionCode.WEBP_CONVERT_FAILED);
            }

            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            throw new ImageException(ImageExceptionCode.IMAGE_IO_ERROR, e, "WebP 변환");
        }
    }

    private void validateImageSize(MultipartFile file) {
        long maxSizeBytes = imageProperties.getMaxSizeBytes();
        if (file.getSize() > maxSizeBytes) {
            throw new ImageException(ImageExceptionCode.INVALID_IMAGE_SIZE, maxSizeBytes);
        }
    }

    private void validateImageContentType(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new ImageException(
                    ImageExceptionCode.UNSUPPORTED_IMAGE_CONTENT_TYPE,
                    contentType
            );
        }
    }

    private void validateExtension(String originalFilename) {
        if (originalFilename == null || !originalFilename.contains(".")) {
            throw new ImageException(ImageExceptionCode.MISSING_EXTENSION);
        }

        String extension = extractExtension(originalFilename).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new ImageException(
                    ImageExceptionCode.UNSUPPORTED_EXTENSION,
                    extension
            );
        }
    }

    private String extractExtension(String originalFilename) {
        if (originalFilename == null || !originalFilename.contains(".")) {
            return "";
        }
        return originalFilename.substring(originalFilename.lastIndexOf("."));
    }

    private void validateDir(String dir) {
        // TODO: 모임 경로: FE 요청으로 루트 디렉토리로 개선했습니다.
//        if (dir == null || dir.isBlank()) {
//            throw new ImageException(ImageExceptionCode.DIR_REQUIRED);
//        }

//        if (!dir.matches("[a-zA-Z0-9_\\-/]+")) {
//            throw new ImageException(ImageExceptionCode.DIR_INVALID_PATTERN);
//        }

        if (dir.contains("..") || dir.startsWith("/")) {
            throw new ImageException(ImageExceptionCode.DIR_INVALID_TRAVERSAL);
        }

        if (dir.endsWith("/")) {
            throw new ImageException(ImageExceptionCode.DIR_TRAILING_SLASH);
        }
    }

    private String buildKey(String dir, String originalFilename, int index) {
        String extension = extractExtension(originalFilename);
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String uuid = UUID.randomUUID().toString();

        return dir + "/" + timestamp + "_" + index + "_" + uuid + extension;
    }

    private String buildKey(String originalFilename, int index) {
        String extension = extractExtension(originalFilename);
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String uuid = UUID.randomUUID().toString();

        return timestamp + "_" + index + "_" + uuid + extension;
    }


    private String buildBaseName(int index) {
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String uuid = UUID.randomUUID().toString();
        return timestamp + "_" + index + "_" + uuid;
    }

    private byte[] resizeIfNeededKeepFormat(MultipartFile file) {
        return resizeToBox(
                file,
                imageProperties.getMaxWidth(),
                imageProperties.getMaxHeight(),
                "이미지 리사이즈"
        );
    }

    private byte[] resizeToBox(
            MultipartFile file,
            int targetMaxWidth,
            int targetMaxHeight,
            String errorPrefix
    ) {
        try {
            BufferedImage originalImage = ImageIO.read(file.getInputStream());
            if (originalImage == null) {
                throw new ImageException(ImageExceptionCode.INVALID_IMAGE_FORMAT);
            }

            int width = originalImage.getWidth();
            int height = originalImage.getHeight();

            if (width <= targetMaxWidth && height <= targetMaxHeight) {
                return file.getBytes();
            }

            BufferedImage resized = Thumbnails.of(originalImage)
                    .size(targetMaxWidth, targetMaxHeight)
                    .asBufferedImage();

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            String formatName = getFormatName(file.getOriginalFilename());

            Thumbnails.of(resized)
                    .size(resized.getWidth(), resized.getHeight())
                    .outputFormat(formatName)
                    .toOutputStream(byteArrayOutputStream);

            if (byteArrayOutputStream.size() == 0) {
                throw new ImageException(
                        ImageExceptionCode.RESIZE_FAILED,
                        errorPrefix,
                        formatName
                );
            }

            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            throw new ImageException(ImageExceptionCode.IMAGE_IO_ERROR, e, errorPrefix);
        }
    }

    private String getFormatName(String originalFilename) {
        if (originalFilename == null || !originalFilename.contains(".")) {
            return "jpg";
        }
        String ext = originalFilename.substring(originalFilename.lastIndexOf('.') + 1)
                .toLowerCase();

        if ("jpeg".equals(ext)) {
            return "jpg";
        }
        return ext;
    }

    private void putToS3(String key, byte[] bytes, String contentType) {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(awsS3Properties.getBucket())
                .key(key)
                .contentType(contentType)
                .acl(ObjectCannedACL.PUBLIC_READ)
                .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromBytes(bytes));
    }
}
