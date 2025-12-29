package team.wego.wegobackend.chat.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import team.wego.wegobackend.chat.application.dto.request.CreateDmRequest;
import team.wego.wegobackend.chat.application.dto.request.KickParticipantRequest;
import team.wego.wegobackend.chat.application.dto.response.ChatRoomListResponse;
import team.wego.wegobackend.chat.application.dto.response.ChatRoomResponse;
import team.wego.wegobackend.chat.application.dto.response.MessageListResponse;
import team.wego.wegobackend.chat.application.dto.response.ParticipantListResponse;
import team.wego.wegobackend.chat.application.dto.response.ReadStatusResponse;
import team.wego.wegobackend.common.response.ApiResponse;
import team.wego.wegobackend.common.security.CustomUserDetails;

@Tag(name = "채팅 API", description = "채팅방 및 메시지 관련 API")
public interface ChatRoomControllerDocs {

    @Operation(
            summary = "내 채팅방 목록 조회",
            description = "현재 로그인한 사용자가 참여 중인 모든 채팅방 목록을 조회합니다. "
                    + "각 채팅방의 마지막 메시지, 안읽은 메시지 수 등을 포함합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "조회 성공"
            )
    })
    ResponseEntity<ApiResponse<ChatRoomListResponse>> getMyChatRooms(
            @AuthenticationPrincipal CustomUserDetails userDetails
    );

    @Operation(
            summary = "채팅방 상세 조회",
            description = "특정 채팅방의 상세 정보와 참여자 목록을 조회합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "조회 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "채팅방에 참여하지 않은 사용자"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "채팅방을 찾을 수 없음"
            )
    })
    ResponseEntity<ApiResponse<ChatRoomResponse>> getChatRoom(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "채팅방 ID", required = true) @PathVariable Long roomId
    );

    @Operation(
            summary = "메시지 이력 조회",
            description = "채팅방의 메시지 이력을 커서 기반 페이징으로 조회합니다. "
                    + "cursor가 없으면 최신 메시지부터 조회합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "조회 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "채팅방에 참여하지 않은 사용자"
            )
    })
    ResponseEntity<ApiResponse<MessageListResponse>> getMessages(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "채팅방 ID", required = true) @PathVariable Long roomId,
            @Parameter(description = "페이징 커서 (이전 응답의 nextCursor 값)") @RequestParam(required = false) Long cursor,
            @Parameter(description = "조회할 메시지 수 (기본값: 50)") @RequestParam(defaultValue = "50") int size
    );

    @Operation(
            summary = "읽음 처리",
            description = "채팅방의 모든 메시지를 읽음 처리합니다. "
                    + "마지막으로 읽은 메시지 ID가 업데이트됩니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "읽음 처리 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "채팅방에 참여하지 않은 사용자"
            )
    })
    ResponseEntity<ApiResponse<ReadStatusResponse>> markAsRead(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "채팅방 ID", required = true) @PathVariable Long roomId
    );

    @Operation(
            summary = "참여자 추방",
            description = "채팅방에서 특정 참여자를 추방합니다. 그룹 채팅방의 경우 방장만 사용할 수 있습니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "추방 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "자기 자신을 추방할 수 없음"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "방장만 사용할 수 있는 기능"
            )
    })
    ResponseEntity<ApiResponse<Void>> kickParticipant(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "채팅방 ID", required = true) @PathVariable Long roomId,
            @Valid @RequestBody KickParticipantRequest request
    );

    @Operation(
            summary = "채팅방 나가기",
            description = "채팅방에서 퇴장합니다. 퇴장 후에는 해당 채팅방에 접근할 수 없습니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "퇴장 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "채팅방에 참여하지 않은 사용자"
            )
    })
    ResponseEntity<ApiResponse<Void>> leaveChatRoom(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "채팅방 ID", required = true) @PathVariable Long roomId
    );

    @Operation(
            summary = "1:1 채팅방 생성/조회",
            description = "특정 사용자와의 1:1 채팅방을 생성하거나 기존 채팅방을 조회합니다. "
                    + "이미 해당 사용자와의 DM 채팅방이 있으면 기존 채팅방을 반환합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "채팅방 생성/조회 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "자기 자신에게 메시지를 보낼 수 없음"
            )
    })
    ResponseEntity<ApiResponse<ChatRoomResponse>> createOrGetDmRoom(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CreateDmRequest request
    );

    @Operation(
            summary = "채팅방 참여자 목록 조회",
            description = "채팅방의 활성 참여자 목록을 조회합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "조회 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "채팅방에 참여하지 않은 사용자"
            )
    })
    ResponseEntity<ApiResponse<ParticipantListResponse>> getParticipants(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "채팅방 ID", required = true) @PathVariable Long roomId
    );
}
