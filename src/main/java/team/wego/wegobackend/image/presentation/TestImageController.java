package team.wego.wegobackend.image.presentation;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import team.wego.wegobackend.common.infrastructure.aws.image.UploadedImage;
import team.wego.wegobackend.common.infrastructure.aws.image.ImageStorageService;

@RestController
@RequestMapping("/api/v1/test/images")
@RequiredArgsConstructor
public class TestImageController {

    private final ImageStorageService imageStorageService;

    @PostMapping(value = "/one", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UploadedImageResponse uploadOne(@RequestPart("file") MultipartFile file) {
        UploadedImage uploaded = imageStorageService.uploadImage("test", file, 0);
        return UploadedImageResponse.from(uploaded);

    }

    @PostMapping(value = "/many", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public List<UploadedImageResponse> uploadMany(@RequestPart("files") List<MultipartFile> files) {
        return imageStorageService.uploadAll("test", files).stream()
                .map(UploadedImageResponse::from)
                .toList();
    }

    @DeleteMapping
    public void deleteOne(@RequestParam("key") String key) {
        imageStorageService.deleteObject(key);
    }

    @DeleteMapping("/many")
    public void deleteMany(@RequestParam("keys") List<String> keys) {
        imageStorageService.deleteObjects(keys);
    }
}
