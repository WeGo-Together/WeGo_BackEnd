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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import team.wego.wegobackend.image.domain.ImageFile;

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

    @Value("${aws.s3.bucket}")
    private String bucket;

    @Value("${aws.s3.public-endpoint}")
    private String publicEndpoint;

    @Value("${image.max-size-bytes}")
    private long maxSizeBytes;

    @Value("${image.max-width}")
    private int maxWidth;

    @Value("${image.max-height}")
    private int maxHeight;

    @Value("${image.thumb-max-width}")
    private int thumbMaxWidth;

    @Value("${image.thumb-max-height}")
    private int thumbMaxHeight;

    public ImageFile uploadOriginal(String dir, MultipartFile file, int index) {
        validateImageSize(file);
        validateImageContentType(file);
        validateExtension(file.getOriginalFilename());

        String originalFilename = file.getOriginalFilename();
        String key = buildKey(dir, originalFilename, index);

        byte[] bytes = resizeIfNeededKeepFormat(file);

        putToS3(key, bytes, file.getContentType());
        String url = publicEndpoint + "/" + key;

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

    public ImageFile uploadAsWebp(String dir, MultipartFile file, int index) {
        validateImageSize(file);
        validateImageContentType(file);
        validateExtension(file.getOriginalFilename());

        String baseName = buildBaseName(index);
        String key = dir + "/" + baseName + ".webp";

        byte[] bytes = convertToWebp(file);

        putToS3(key, bytes, "image/webp");
        String url = publicEndpoint + "/" + key;

        return new ImageFile(key, url);
    }

    public List<ImageFile> uploadAllAsWebp(String dir, List<MultipartFile> files) {
        List<ImageFile> result = new ArrayList<>();
        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);
            result.add(uploadAsWebp(dir, file, i));
        }
        return result;
    }

    public ImageFile uploadThumb(String dir, MultipartFile file, int index) {
        validateImageSize(file);
        validateImageContentType(file);
        validateExtension(file.getOriginalFilename());

        String originalFilename = file.getOriginalFilename();
        String key = buildKey(dir, originalFilename, index);

        byte[] bytes = resizeToThumb(file);

        putToS3(key, bytes, file.getContentType());
        String url = publicEndpoint + "/" + key;

        return new ImageFile(key, url);
    }

    public void delete(String key) {
        s3Client.deleteObject(builder -> builder.bucket(bucket).key(key));
    }

    public void deleteAll(List<String> keys) {
        for (String key : keys) {
            delete(key);
        }
    }

    private void validateImageSize(MultipartFile file) {
        if (file.getSize() > maxSizeBytes) {
            throw new IllegalArgumentException(
                    "이미지 크기가 너무 큽니다. 최대 " + maxSizeBytes + " bytes 까지만 허용됩니다."
            );
        }
    }

    private void validateImageContentType(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("허용되지 않은 이미지 타입입니다: " + contentType);
        }
    }

    private void validateExtension(String originalFilename) {
        if (originalFilename == null || !originalFilename.contains(".")) {
            throw new IllegalArgumentException("확장자가 없는 파일입니다.");
        }

        String extension = extractExtension(originalFilename).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("허용되지 않은 확장자입니다: " + extension);
        }
    }

    private String extractExtension(String originalFilename) {
        if (originalFilename == null || !originalFilename.contains(".")) {
            return "";
        }
        return originalFilename.substring(originalFilename.lastIndexOf("."));
    }

    private String buildKey(String dir, String originalFilename, int index) {
        String extension = extractExtension(originalFilename);
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String uuid = UUID.randomUUID().toString();

        return dir + "/" + timestamp + "_" + index + "_" + uuid + extension;
    }

    private String buildBaseName(int index) {
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String uuid = UUID.randomUUID().toString();
        return timestamp + "_" + index + "_" + uuid;
    }

    private byte[] convertToWebp(MultipartFile file) {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            Thumbnails.of(file.getInputStream())
                    .size(maxWidth, maxHeight)
                    .outputFormat("webp")
                    .toOutputStream(byteArrayOutputStream);

            if (byteArrayOutputStream.size() == 0) {
                throw new IllegalStateException("WebP 변환에 실패했습니다.");
            }

            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("WebP 변환 중 오류가 발생했습니다.", e);
        }
    }

    private void putToS3(String key, byte[] bytes, String contentType) {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(contentType)
                .acl(ObjectCannedACL.PUBLIC_READ)
                .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromBytes(bytes));
    }

    private byte[] resizeIfNeededKeepFormat(MultipartFile file) {
        return resizeToBox(file, maxWidth, maxHeight, "이미지 리사이즈");
    }

    private byte[] resizeToThumb(MultipartFile file) {
        return resizeToBox(file, thumbMaxWidth, thumbMaxHeight, "썸네일 생성");
    }

    private byte[] resizeToBox(
            MultipartFile file,
            int targetMaxWidth,
            int targetMaxHeight,
            String errorPrefix
    ) {
        // TODO: IO Exception 부분. 공통 예외로 구현으로 try-catch 제거를 노려보자.
        try {
            BufferedImage originalImage = ImageIO.read(file.getInputStream());
            if (originalImage == null) {
                throw new IllegalArgumentException("이미지 파일 형식이 올바르지 않습니다.");
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
                throw new IllegalStateException(errorPrefix + "에 실패했습니다. format=" + formatName);
            }

            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(errorPrefix + " 중 오류가 발생했습니다.", e);
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
}
