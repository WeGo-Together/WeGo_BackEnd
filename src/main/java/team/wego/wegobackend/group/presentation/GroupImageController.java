package team.wego.wegobackend.group.presentation;


import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import team.wego.wegobackend.common.response.ApiResponse;
import team.wego.wegobackend.common.security.CustomUserDetails;
import team.wego.wegobackend.group.application.dto.request.UpdateGroupImageItemRequest;
import team.wego.wegobackend.group.application.dto.response.GroupImageItemResponse;
import team.wego.wegobackend.group.application.dto.response.PreUploadGroupImageResponse;
import team.wego.wegobackend.group.application.service.GroupImageService;

@RequiredArgsConstructor
@RequestMapping("/api/v1/groups/images")
@RestController
public class GroupImageController {

    private final GroupImageService groupImageService;

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<PreUploadGroupImageResponse>> uploadImages(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam("images") List<MultipartFile> images
    ) {
        PreUploadGroupImageResponse response = groupImageService.uploadGroupImages(
                images);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED.value(), response));
    }

    @PatchMapping("/{groupId}")
    public ResponseEntity<ApiResponse<List<GroupImageItemResponse>>> updateGroupImages(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long groupId,
            @RequestBody @Valid List<UpdateGroupImageItemRequest> images
    ) {
        List<GroupImageItemResponse> response =
                groupImageService.updateGroupImages(userDetails, groupId, images);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(HttpStatus.OK.value(), response));
    }


    @DeleteMapping("/{groupId}")
    public ResponseEntity<ApiResponse<Void>> deleteGroupImages(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long groupId
    ) {
        groupImageService.deleteGroupImages(userDetails, groupId);

        return ResponseEntity.noContent().build();
    }

}
