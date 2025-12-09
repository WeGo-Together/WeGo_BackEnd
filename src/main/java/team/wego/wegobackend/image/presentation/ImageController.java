package team.wego.wegobackend.image.presentation;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import team.wego.wegobackend.common.response.ApiResponse;
import team.wego.wegobackend.image.application.dto.ImageFileResponse;
import team.wego.wegobackend.image.application.service.ImageUploadService;
import team.wego.wegobackend.image.domain.ImageFile;
import team.wego.wegobackend.image.domain.ImageSize;

@RestController
@RequestMapping("/api/v1/images")
@RequiredArgsConstructor
public class ImageController {

    private final ImageUploadService imageUploadService;

    @PostMapping(
            value = "/original",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ApiResponse<ImageFileResponse>> uploadOriginal(
            @RequestParam("dir") String dir,
            @RequestPart("file") MultipartFile file
    ) {
        ImageFile image = imageUploadService.uploadOriginal(dir, file, 0);
        ImageFileResponse response = ImageFileResponse.from(image);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(201, response));
    }

    @PostMapping(
            value = "/originals",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ApiResponse<List<ImageFileResponse>>> uploadOriginals(
            @RequestParam("dir") String dir,
            @RequestPart("files") List<MultipartFile> files
    ) {
        List<ImageFileResponse> responses = imageUploadService.uploadAllOriginal(dir, files)
                .stream()
                .map(ImageFileResponse::from)
                .toList();

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(201, responses));
    }

    /**
     * 단일 크기로 WebP 업로드 (예: 440x240, 100x100 등)
     */
    @PostMapping(
            value = "/webp",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ApiResponse<ImageFileResponse>> uploadAsWebpWithSize(
            @RequestParam("dir") String dir,
            @RequestParam("width") int width,
            @RequestParam("height") int height,
            @RequestPart("file") MultipartFile file
    ) {
        ImageSize size = new ImageSize(width, height);
        ImageFile image = imageUploadService.uploadAsWebpWithSize(dir, file, 0, size);
        ImageFileResponse response = ImageFileResponse.from(image);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(201, response));
    }

    @PostMapping(
            value = "/webp/multiple",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ApiResponse<List<ImageFileResponse>>> uploadAsWebpWithSizes(
            @RequestParam("dir") String dir,
            @RequestParam("widths") List<Integer> widths,
            @RequestParam("heights") List<Integer> heights,
            @RequestPart("file") MultipartFile file
    ) {
        List<ImageFileResponse> responses = imageUploadService
                .uploadAsWebpWithSizes(dir, file, 0, widths, heights)
                .stream()
                .map(ImageFileResponse::from)
                .toList();

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(201, responses));
    }

    @DeleteMapping("/one")
    public ResponseEntity<Void> deleteOne(@RequestParam("key") String key) {
        imageUploadService.delete(key);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteMany(@RequestParam("keys") List<String> keys) {
        imageUploadService.deleteAll(keys);

        return ResponseEntity.noContent().build();
    }
}
