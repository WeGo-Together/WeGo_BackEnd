package team.wego.wegobackend.group.presentation;


import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import team.wego.wegobackend.common.response.ApiResponse;
import team.wego.wegobackend.group.application.dto.response.PreUploadGroupImageResponse;
import team.wego.wegobackend.group.application.service.GroupImageService;

@RequiredArgsConstructor
@RequestMapping("/api/v1/groups/images")
@RestController
public class GroupImageController {

    private final GroupImageService groupImageService;

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<PreUploadGroupImageResponse>> uploadImages(
            @RequestParam("images") List<MultipartFile> images
    ) {
        PreUploadGroupImageResponse response = groupImageService.uploadGroupImages(
                images);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    // 모임 이미지 수정


}
