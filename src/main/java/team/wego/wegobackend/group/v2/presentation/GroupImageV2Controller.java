package team.wego.wegobackend.group.v2.presentation;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import team.wego.wegobackend.common.response.ApiResponse;
import team.wego.wegobackend.common.security.CustomUserDetails;
import team.wego.wegobackend.group.v2.application.dto.response.PreUploadGroupImageV2Response;
import team.wego.wegobackend.group.v2.application.service.GroupImageV2PreUploadService;

@RequiredArgsConstructor
@RequestMapping("/api/v2/groups/images")
@RestController
public class GroupImageV2Controller implements GroupImageV2ControllerDocs {

    private final GroupImageV2PreUploadService preUploadService;


    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<PreUploadGroupImageV2Response>> uploadImages(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam("images") List<MultipartFile> images
    ) {
        PreUploadGroupImageV2Response response = preUploadService.uploadGroupImagesV2(
                userDetails.getId(), images);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED.value(), response));
    }
}
