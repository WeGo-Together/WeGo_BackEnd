package team.wego.wegobackend.group.v2.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import team.wego.wegobackend.common.response.ApiResponse;
import team.wego.wegobackend.common.security.CustomUserDetails;
import team.wego.wegobackend.group.v2.application.dto.response.PreUploadGroupImageV2Response;

@Tag(name = "V2 모임 이미지 API", description = "V2 모임 이미지 업로드/수정/삭제와 관련된 API 리스트 🍃")
public interface GroupImageV2ControllerDocs {

    @Operation(
            summary = "V2 모임 이미지 사전 업로드 API",
            description = """
                    모임에 사용할 이미지를 업로드하기 전에, S3 등의 스토리지에 미리 업로드하고
                    그 결과(이미지 URL, 키 등)를 응답으로 반환합니다.
                    
                    - images(Arrays): 업로드할 이미지 파일 리스트 (최소 1개 이상)
                    """
    )
    ResponseEntity<ApiResponse<PreUploadGroupImageV2Response>> uploadImages(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam("images") List<MultipartFile> images
    );
}
