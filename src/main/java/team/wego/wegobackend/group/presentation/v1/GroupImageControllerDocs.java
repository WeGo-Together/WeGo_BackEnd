package team.wego.wegobackend.group.presentation.v1;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import team.wego.wegobackend.common.response.ApiResponse;
import team.wego.wegobackend.common.security.CustomUserDetails;
import team.wego.wegobackend.group.application.dto.v1.request.UpdateGroupImageItemRequest;
import team.wego.wegobackend.group.application.dto.v1.response.GroupImageItemResponse;
import team.wego.wegobackend.group.application.dto.v1.response.PreUploadGroupImageResponse;

@Tag(name = "V2 사용 필수: V1 모임 이미지 API", description = "V1 모임 이미지 업로드/수정/삭제와 관련된 API 리스트 🐈")
public interface GroupImageControllerDocs {

    @Operation(
            summary = "모임 이미지 사전 업로드 API",
            description = """
                모임에 사용할 이미지를 업로드하기 전에, S3 등의 스토리지에 미리 업로드하고
                그 결과(이미지 URL, 키 등)를 응답으로 반환합니다.
                
                - images: 업로드할 이미지 파일 리스트 (최소 1개 이상)
                """
    )
    ResponseEntity<ApiResponse<PreUploadGroupImageResponse>> uploadImages(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam("images") List<MultipartFile> images
    );

    @Operation(
            summary = "모임 이미지 정보 업데이트 API",
            description = """
                특정 모임에 연결된 이미지 정보들을 업데이트합니다.
                주로 사전 업로드된 이미지 URL/키를 모임에 매핑하거나,
                대표 이미지 여부, 정렬 순서 등을 수정할 때 사용합니다.
                
                - groupId: 이미지를 수정할 모임 ID
                - body: 이미지 항목별 수정 정보 리스트
                """
    )
    ResponseEntity<ApiResponse<List<GroupImageItemResponse>>> updateGroupImages(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long groupId,
            @RequestBody @Valid List<UpdateGroupImageItemRequest> images
    );

    @Operation(
            summary = "모임 이미지 삭제 API",
            description = """
                특정 모임에 연결된 이미지들을 삭제합니다.
                모임 호스트 또는 권한이 있는 유저만 삭제할 수 있습니다.
                
                - groupId: 이미지를 삭제할 모임 ID
                """
    )
    ResponseEntity<ApiResponse<Void>> deleteGroupImages(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long groupId
    );
}
