package team.wego.wegobackend.group.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import team.wego.wegobackend.common.response.ApiResponse;
import team.wego.wegobackend.common.security.CustomUserDetails;
import team.wego.wegobackend.group.application.dto.request.CreateGroupRequest;
import team.wego.wegobackend.group.application.dto.request.UpdateGroupRequest;
import team.wego.wegobackend.group.application.dto.response.CreateGroupResponse;
import team.wego.wegobackend.group.application.dto.response.GetGroupListResponse;
import team.wego.wegobackend.group.application.dto.response.GetGroupResponse;

@Tag(name = "모임 API", description = "모임과 관련된 API 리스트 🐈")
public interface GroupControllerDocs {

    @Operation(
            summary = "모임 생성 API",
            description = "새로운 모임을 생성합니다. 로그인한 유저를 모임 호스트로 저장합니다."
    )
    ResponseEntity<ApiResponse<CreateGroupResponse>> createGroupResponse(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid CreateGroupRequest request
    );

    @Operation(
            summary = "모임 참가 API",
            description = "특정 모임에 참가합니다. 이미 참가 중인 경우 예외가 발생할 수 있습니다."
    )
    ResponseEntity<ApiResponse<GetGroupResponse>> attendGroup(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long groupId
    );

    @Operation(
            summary = "모임 참가 취소 API",
            description = "참가 중인 모임의 참가를 취소합니다."
    )
    ResponseEntity<ApiResponse<GetGroupResponse>> cancelAttendGroup(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long groupId
    );

    @Operation(
            summary = "모임 상세 조회 API",
            description = """
                특정 모임의 상세 정보를 조회합니다.
                비회원도 조회 가능하며, 로그인한 유저일 경우 참가 여부/호스트 여부 등 추가 정보가 포함될 수 있습니다.
                """
    )
    ResponseEntity<ApiResponse<GetGroupResponse>> getGroupResponse(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long groupId
    );

    @Operation(
            summary = "모임 목록 조회 API",
            description = """
                모임 리스트를 조회합니다.
                검색 키워드와 커서 기반 페이징을 지원합니다.
                - keyword: 모임 제목/내용 검색에 사용되는 선택값
                - cursor: 마지막으로 조회한 모임 ID(커서 기반 페이징)
                - size: 한 번에 조회할 모임 개수
                """
    )
    ResponseEntity<ApiResponse<GetGroupListResponse>> getGroupList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long cursor,
            @RequestParam int size
    );

    @Operation(
            summary = "모임 수정 API",
            description = "기존에 생성된 모임 정보를 수정합니다. 모임 호스트만 수정 가능합니다."
    )
    ResponseEntity<ApiResponse<GetGroupResponse>> updateGroup(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long groupId,
            @RequestBody @Valid UpdateGroupRequest request
    );

    @Operation(
            summary = "모임 삭제 API",
            description = "기존에 생성된 모임을 삭제합니다. 모임 호스트만 삭제 가능합니다."
    )
    ResponseEntity<ApiResponse<Void>> deleteGroup(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long groupId
    );

    @Operation(
            summary = "내 모임 목록 조회 API",
            description = """
                로그인한 유저 기준으로 나와 관련된 모임 목록을 조회합니다.
                type 값에 따라 조회 대상이 달라질 수 있습니다.
                예시)
                - current: 현재 참가 중인 모임
                - host: 내가 호스트인 모임
                - past: 종료된 모임
                """
    )
    ResponseEntity<ApiResponse<GetGroupListResponse>> getMyGroups(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam String type,
            @RequestParam(required = false) Long cursor,
            @RequestParam int size
    );
}
