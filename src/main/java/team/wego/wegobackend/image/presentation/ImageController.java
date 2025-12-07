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
                .body(ApiResponse.success(
                        HttpStatus.CREATED.value(),
                        "이미지: 원본 업로드가 정상적으로 처리되었습니다.",
                        response
                ));
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
                .body(ApiResponse.success(
                        HttpStatus.CREATED.value(),
                        "이미지: 여러 원본 업로드가 정상적으로 처리되었습니다.",
                        responses
                ));
    }

    @PostMapping(
            value = "/webp",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ApiResponse<ImageFileResponse>> uploadAsWebp(
            @RequestParam("dir") String dir,
            @RequestPart("file") MultipartFile file
    ) {
        ImageFile image = imageUploadService.uploadAsWebp(dir, file, 0);
        ImageFileResponse response = ImageFileResponse.from(image);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        HttpStatus.CREATED.value(),
                        "이미지: WebP 변환 업로드가 정상적으로 처리되었습니다.",
                        response
                ));
    }

    @PostMapping(
            value = "/webps",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ApiResponse<List<ImageFileResponse>>> uploadAllAsWebp(
            @RequestParam("dir") String dir,
            @RequestPart("files") List<MultipartFile> files
    ) {
        List<ImageFileResponse> responses = imageUploadService.uploadAllAsWebp(dir, files)
                .stream()
                .map(ImageFileResponse::from)
                .toList();

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        HttpStatus.CREATED.value(),
                        "이미지: 여러 WebP 변환 업로드가 정상적으로 처리되었습니다.",
                        responses
                ));
    }

    @DeleteMapping("/one")
    public ResponseEntity<ApiResponse<Void>> deleteOne(@RequestParam("key") String key) {
        imageUploadService.delete(key);

        return ResponseEntity
                .ok(ApiResponse.success(
                        HttpStatus.CREATED.toString(),
                        "이미지: 단일 삭제가 정상적으로 처리되었습니다."
                ));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> deleteMany(@RequestParam("keys") List<String> keys) {
        imageUploadService.deleteAll(keys);

        return ResponseEntity
                .ok(ApiResponse.success(
                        HttpStatus.OK.value(),
                        "이미지: 여러 건 삭제가 정상적으로 처리되었습니다."
                ));
    }

    @PostMapping(
            value = "/thumb",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ApiResponse<ImageFileResponse>> uploadThumb(
            @RequestParam("dir") String dir,
            @RequestPart("file") MultipartFile file
    ) {
        ImageFile image = imageUploadService.uploadThumb(dir, file, 0);
        ImageFileResponse response = ImageFileResponse.from(image);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        HttpStatus.CREATED.value(),
                        "이미지: 썸네일 업로드가 정상적으로 처리되었습니다.",
                        response
                ));
    }
}
