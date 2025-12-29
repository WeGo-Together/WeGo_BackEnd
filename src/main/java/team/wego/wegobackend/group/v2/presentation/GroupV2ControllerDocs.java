package team.wego.wegobackend.group.v2.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import team.wego.wegobackend.common.response.ApiResponse;
import team.wego.wegobackend.common.security.CustomUserDetails;
import team.wego.wegobackend.group.v2.application.dto.request.AttendGroupV2Request;
import team.wego.wegobackend.group.v2.application.dto.request.CreateGroupV2Request;
import team.wego.wegobackend.group.v2.application.dto.request.GroupListFilter;
import team.wego.wegobackend.group.v2.application.dto.request.UpdateGroupV2Request;
import team.wego.wegobackend.group.v2.application.dto.response.AttendanceGroupV2Response;
import team.wego.wegobackend.group.v2.application.dto.response.CreateGroupV2Response;
import team.wego.wegobackend.group.v2.application.dto.response.GetBanTargetsResponse;
import team.wego.wegobackend.group.v2.application.dto.response.GetBannedTargetsResponse;
import team.wego.wegobackend.group.v2.application.dto.response.GetGroupListV2Response;
import team.wego.wegobackend.group.v2.application.dto.response.GetGroupV2Response;
import team.wego.wegobackend.group.v2.application.dto.response.GetKickTargetsResponse;
import team.wego.wegobackend.group.v2.application.dto.response.GetMyGroupListV2Response;
import team.wego.wegobackend.group.v2.application.dto.response.GroupJoinRequestsResponse;
import team.wego.wegobackend.group.v2.application.dto.response.GroupUserV2StatusResponse;
import team.wego.wegobackend.group.v2.application.dto.response.UpdateGroupV2Response;
import team.wego.wegobackend.group.v2.domain.entity.GroupUserV2Status;
import team.wego.wegobackend.group.v2.domain.entity.GroupV2Status;

@Tag(name = "V2 모임 API", description = "V2 모임과 관련된 API 리스트 🍃")
public interface GroupV2ControllerDocs {

    @Operation(
            summary = "모임 생성 API",
            description = "새로운 모임을 생성합니다. 로그인한 유저를 모임 호스트로 저장합니다."
    )
    ResponseEntity<ApiResponse<CreateGroupV2Response>> create(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid CreateGroupV2Request request
    );

    @Operation(
            summary = "모임 상세 조회 API",
            description = """
                    특정 모임의 상세 정보를 조회합니다.
                    비회원도 조회 가능하며, 로그인한 유저일 경우 참가 여부/호스트 여부 등 추가 정보가 포함될 수 있습니다.
                    """
    )
    ResponseEntity<ApiResponse<GetGroupV2Response>> getGroup(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long groupId
    );

    @Operation(
            summary = "모임 참가 API",
            description = "특정 모임에 참가합니다. 이미 참가 중인 경우 예외가 발생할 수 있습니다."
    )
    ResponseEntity<ApiResponse<AttendanceGroupV2Response>> attend(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long groupId,
            @RequestBody(required = false) AttendGroupV2Request request
    );


    @Operation(
            summary = "모임 참가 취소 API",
            description = "참가 중인 모임의 참가를 취소합니다."
    )
    ResponseEntity<ApiResponse<AttendanceGroupV2Response>> left(
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
                    - filter: 모임 상태 필터(ACTIVE, ARCHIVED, FULL)
                    - includeStatuses: 포함할 모임 상태(RECRUITING, FULL, CLOSED, CANCELLED, FINISHED)
                    - excludeStatuses: 제외할 모임 상태(RECRUITING, FULL, CLOSED, CANCELLED, FINISHED)
                      - includeStatuses가 있으면 filter의 기본 include는 무시됩니다.
                      - excludeStatuses가 있으면 해당 상태는 제외됩니다.
                      - include와 exclude가 동시에 충돌하면 exclude가 우선됩니다.
                    """
    )
    ResponseEntity<ApiResponse<GetGroupListV2Response>> getGroupList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "ACTIVE") GroupListFilter filter,
            @RequestParam(required = false) List<GroupV2Status> includeStatuses,
            @RequestParam(required = false) List<GroupV2Status> excludeStatuses
    );

    @Operation(
            summary = "모임 수정 API",
            description = "기존에 생성된 모임 정보를 수정합니다. 모임 호스트만 수정 가능합니다."
    )
    ResponseEntity<ApiResponse<UpdateGroupV2Response>> update(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long groupId,
            @Valid @RequestBody UpdateGroupV2Request request
    );

    @Operation(
            summary = "내 모임 목록 조회 API",
            description = """
                    로그인한 유저 기준으로 나와 관련된 모임 목록을 조회합니다.
                    type 값에 따라 조회 대상이 달라질 수 있습니다.
                    type: current / past / myPost
                    - current: 현재 참가 중인 모임
                    - host: 내가 호스트인 모임
                    - past: 종료된 모임
                    
                    - cursor: 마지막으로 조회한 모임 ID(커서 기반 페이징)
                    - size: 한 번에 조회할 모임 개수
                    - filter: 모임 상태 필터(ACTIVE, ARCHIVED, FULL)
                    - includeStatuses: 포함할 모임 상태(RECRUITING, FULL, CLOSED, CANCELLED, FINISHED)
                    - excludeStatuses: 제외할 모임 상태(RECRUITING, FULL, CLOSED, CANCELLED, FINISHED)
                      - includeStatuses가 있으면 filter의 기본 include는 무시됩니다.
                      - excludeStatuses가 있으면 해당 상태는 제외됩니다.
                      - include와 exclude가 동시에 충돌하면 exclude가 우선됩니다.
                    - myStatuses: 내 참여 상태(ATTEND, LEFT, KICKED,BANNED)
                    """
    )
    ResponseEntity<ApiResponse<GetMyGroupListV2Response>> getMyGroups(
            @AuthenticationPrincipal CustomUserDetails userDetails,

            @RequestParam(required = false) String type,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "20") int size,

            @RequestParam(required = false) GroupListFilter filter,
            @RequestParam(required = false) List<GroupV2Status> includeStatuses,
            @RequestParam(required = false) List<GroupV2Status> excludeStatuses,

            @RequestParam(required = false) List<GroupUserV2Status> myStatuses);


    @Operation(
            summary = "모임 삭제 API",
            description = "기존에 생성된 모임을 삭제합니다. 모임 호스트만 삭제 가능합니다."
    )
    ResponseEntity<Void> delete(@AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long groupId);


    @Operation(
            summary = "승인 API (HOST/권한자)",
            description = """
                    승인제(APPROVAL_REQUIRED) 모임에서 PENDING 상태의 참여 신청자를 승인합니다.
                    - PENDING -> ATTEND
                    - 권한: HOST 또는 정책상 승인 가능한 권한자
                    """
    )
    @PostMapping("/{groupId}/attendance/{targetUserId}/approve")
    ResponseEntity<ApiResponse<GroupUserV2StatusResponse>> approve(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long groupId,
            @PathVariable Long targetUserId
    );

    @Operation(
            summary = "거절 API (HOST/권한자)",
            description = """
                    승인제(APPROVAL_REQUIRED) 모임에서 PENDING 상태의 참여 신청자를 거절합니다.
                    - PENDING -> REJECTED
                    - 권한: HOST 또는 정책상 거절 가능한 권한자
                    """
    )
    @PostMapping("/{groupId}/attendance/{targetUserId}/reject")
    ResponseEntity<ApiResponse<GroupUserV2StatusResponse>> reject(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long groupId,
            @PathVariable Long targetUserId
    );

    @Operation(
            summary = "강퇴 API (HOST)",
            description = """
                    모임 참여자(ATTEND)를 강퇴합니다.
                    - ATTEND -> KICKED
                    - 권한: HOST
                    """
    )
    @PostMapping("/{groupId}/attendance/{targetUserId}/kick")
    ResponseEntity<ApiResponse<GroupUserV2StatusResponse>> kick(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long groupId,
            @PathVariable Long targetUserId
    );

    @Operation(
            summary = "차단(BAN) API (HOST)",
            description = """
                    모임 참여자(ATTEND)를 차단합니다.
                    - ATTEND -> BANNED
                    - 권한: HOST
                    """
    )
    @PostMapping("/{groupId}/attendance/{targetUserId}/ban")
    ResponseEntity<ApiResponse<GroupUserV2StatusResponse>> ban(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long groupId,
            @PathVariable Long targetUserId
    );

    @Operation(
            summary = "차단 해제(UNBAN) API (HOST)",
            description = """
                    차단(BANNED) 상태의 유저를 차단 해제합니다.
                    - BANNED -> KICKED (재참여는 유저가 attend로 진행)
                    - 권한: HOST
                    """
    )
    @PostMapping("/{groupId}/attendance/{targetUserId}/unban")
    ResponseEntity<ApiResponse<GroupUserV2StatusResponse>> unban(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long groupId,
            @PathVariable Long targetUserId
    );

    @Operation(
            summary = "강퇴 대상 조회 (HOST)",
            description = """
                    강퇴 가능한 대상(현재 ATTEND 상태, HOST 제외)을 조회합니다.
                    - 권한: HOST
                    """
    )
    @GetMapping("/{groupId}/attendance/kick-targets")
    ResponseEntity<ApiResponse<GetKickTargetsResponse>> getKickTargets(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long groupId
    );

    @Operation(
            summary = "차단 대상 조회 (HOST)",
            description = """
                    차단 가능한 대상(현재 ATTEND 상태, HOST 제외)을 조회합니다.
                    - 권한: HOST
                    """
    )
    @GetMapping("/{groupId}/attendance/ban-targets")
    ResponseEntity<ApiResponse<GetBanTargetsResponse>> getBanTargets(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long groupId
    );

    @Operation(
            summary = "차단된 대상 조회 (HOST)",
            description = """
                    차단(BANNED)된 대상 목록(HOST 제외)을 조회합니다.
                    - 권한: HOST
                    """
    )
    @GetMapping("/{groupId}/attendance/banned-targets")
    ResponseEntity<ApiResponse<GetBannedTargetsResponse>> getBannedTargets(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long groupId
    );

    @Operation(
            summary = "참여 요청 목록 조회 (HOST)",
            description = """
                승인제(APPROVAL_REQUIRED) 모임에서 참여 요청 목록을 조회합니다.
                - 권한: HOST만 조회 가능
                - status 파라미터로 조회 상태를 필터링합니다. (기본값: PENDING)
                - 응답 items에는 joinRequestMessage(신청 메시지)가 포함됩니다.
                """
    )
    @GetMapping("/{groupId}/attendance")
    ResponseEntity<ApiResponse<GroupJoinRequestsResponse>> getJoinRequests(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long groupId,
            @RequestParam(defaultValue = "PENDING") GroupUserV2Status status
    );
}
