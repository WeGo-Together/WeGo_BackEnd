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
import team.wego.wegobackend.common.infrastructure.aws.image.ImageStorageService;
import team.wego.wegobackend.common.infrastructure.aws.image.UploadedImage;
import team.wego.wegobackend.common.infrastructure.aws.image.UploadedImagePair;
import team.wego.wegobackend.image.dto.UploadedImagePairResponse;
import team.wego.wegobackend.image.dto.UploadedImageResponse;

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
    public List<UploadedImageResponse> uploadMany(
            @RequestPart("files") List<MultipartFile> files
    ) {
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

    @PostMapping(value = "/one-with-thumb", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UploadedImagePairResponse uploadOneWithThumb(
            @RequestPart("file") MultipartFile file
    ) {
        UploadedImagePair pair = imageStorageService.uploadWithThumb("test", file, 0);
        return UploadedImagePairResponse.from(pair);
    }

    @PostMapping(value = "/many-with-thumb", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public List<UploadedImagePairResponse> uploadManyWithThumb(
            @RequestPart("files") List<MultipartFile> files
    ) {
        return imageStorageService.uploadAllWithWebpThumb("test", files).stream()
                .map(UploadedImagePairResponse::from)
                .toList();
    }

    @DeleteMapping("/with-thumb")
    public void deleteOneWithThumb(@RequestParam("key") String key) {
        imageStorageService.deleteObjectWithWebpThumb(key);
    }

    @DeleteMapping("/many-with-thumb")
    public void deleteManyWithThumb(@RequestParam("keys") List<String> keys) {
        imageStorageService.deleteObjectsWithWebpThumb(keys);
    }
}
