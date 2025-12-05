package team.wego.wegobackend.common.infrastructure.aws.image;

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

@RequiredArgsConstructor
@Service
public class ImageStorageService {

    private static final List<String> ALLOWED_CONTENT_TYPES = List.of(
            "image/jpeg", "image/png", "image/webp"
    );

    private static final List<String> ALLOWED_EXTENSIONS = List.of(
            ".jpg", ".jpeg", ".png", ".webp"
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

    public UploadedImage uploadImage(String dir, MultipartFile file, int index) {
        validateImageSize(file);
        validateImageContentType(file);
        validateExtension(file.getOriginalFilename());

        String key = buildKey(dir, file.getOriginalFilename(), index);
        byte[] bytes = resizeIfNeeded(file);
        putToS3(key, bytes, file.getContentType());
        String url = publicEndpoint + "/" + key;

        return new UploadedImage(key, url);
    }

    public List<UploadedImage> uploadAll(String dir, List<MultipartFile> files) {
        List<UploadedImage> result = new ArrayList<>();
        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);
            result.add(uploadImage(dir, file, i));
        }
        return result;
    }

    public void deleteObject(String key) {
        s3Client.deleteObject(builder -> builder.bucket(bucket).key(key));
    }

    public void deleteObjects(List<String> keys) {
        for (String key : keys) {
            deleteObject(key);
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

        String extension = originalFilename.substring(originalFilename.lastIndexOf("."))
                .toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("허용되지 않은 확장자입니다: " + extension);
        }
    }

    private byte[] resizeIfNeeded(MultipartFile file) {
        try {
            BufferedImage originalImage = ImageIO.read(file.getInputStream());
            if (originalImage == null) {
                throw new IllegalArgumentException("이미지 파일 형식이 올바르지 않습니다.");
            }

            int width = originalImage.getWidth();
            int height = originalImage.getHeight();

            if (width <= maxWidth && height <= maxHeight) {
                return file.getBytes();
            }

            BufferedImage resized = Thumbnails.of(originalImage)
                    .size(maxWidth, maxHeight)
                    .asBufferedImage();

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            String formatName = getFormatName(file.getOriginalFilename());
            ImageIO.write(resized, formatName, byteArrayOutputStream);
            return byteArrayOutputStream.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("이미지 리사이즈 중 오류가 발생했습니다.", e);
        }
    }

    private String getFormatName(String originalFilename) {
        if (originalFilename == null || !originalFilename.contains(".")) {
            return "jpg";
        }
        String extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1)
                .toLowerCase();
        if ("jpeg".equals(extension)) {
            return "jpg";
        }
        return extension;
    }

    private String buildKey(String dir, String originalFilename, int index) {
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String uuid = UUID.randomUUID().toString();

        return dir + "/" + timestamp + "_" + index + "_" + uuid + extension;
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
}
