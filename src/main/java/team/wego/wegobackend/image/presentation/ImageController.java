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
import team.wego.wegobackend.image.application.service.ImageUploadService;
import team.wego.wegobackend.image.application.dto.ImageFileResponse;
import team.wego.wegobackend.image.domain.ImageFile;

@RestController
@RequestMapping("/api/v1/images")
@RequiredArgsConstructor
public class ImageController {

    private final ImageUploadService imageUploadService;

    @PostMapping(
            value = "/original",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ImageFileResponse uploadOriginal(
            @RequestParam("dir") String dir,
            @RequestPart("file") MultipartFile file
    ) {
        ImageFile image = imageUploadService.uploadOriginal(dir, file, 0);
        return ImageFileResponse.from(image);
    }

    @PostMapping(
            value = "/originals",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public List<ImageFileResponse> uploadOriginals(
            @RequestParam("dir") String dir,
            @RequestPart("files") List<MultipartFile> files
    ) {
        return imageUploadService.uploadAllOriginal(dir, files).stream()
                .map(ImageFileResponse::from)
                .toList();
    }

    @PostMapping(
            value = "/webp",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ImageFileResponse uploadAsWebp(
            @RequestParam("dir") String dir,
            @RequestPart("file") MultipartFile file
    ) {
        ImageFile image = imageUploadService.uploadAsWebp(dir, file, 0);
        return ImageFileResponse.from(image);
    }
    @PostMapping(
            value = "/webps",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public List<ImageFileResponse> uploadAllAsWebp(
            @RequestParam("dir") String dir,
            @RequestPart("files") List<MultipartFile> files
    ) {
        return imageUploadService.uploadAllAsWebp(dir, files).stream()
                .map(ImageFileResponse::from)
                .toList();
    }

    @DeleteMapping("/one")
    public void deleteOne(@RequestParam("key") String key) {
        imageUploadService.delete(key);
    }

    @DeleteMapping
    public void deleteMany(@RequestParam("keys") List<String> keys) {
        imageUploadService.deleteAll(keys);
    }

    @PostMapping(
            value = "/thumb",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ImageFileResponse uploadThumb(
            @RequestParam("dir") String dir,
            @RequestPart("file") MultipartFile file
    ) {
        ImageFile image = imageUploadService.uploadThumb(dir, file, 0);
        return ImageFileResponse.from(image);
    }
}
