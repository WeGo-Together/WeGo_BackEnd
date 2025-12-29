package team.wego.wegobackend.chat.presentation;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import team.wego.wegobackend.chat.application.dto.request.CreateDmRequest;
import team.wego.wegobackend.chat.application.dto.request.KickParticipantRequest;
import team.wego.wegobackend.chat.application.dto.response.ChatRoomListResponse;
import team.wego.wegobackend.chat.application.dto.response.ChatRoomResponse;
import team.wego.wegobackend.chat.application.dto.response.MessageListResponse;
import team.wego.wegobackend.chat.application.dto.response.ParticipantListResponse;
import team.wego.wegobackend.chat.application.dto.response.ReadStatusResponse;
import team.wego.wegobackend.chat.application.service.ChatMessageService;
import team.wego.wegobackend.chat.application.service.ChatRoomService;
import team.wego.wegobackend.common.response.ApiResponse;
import team.wego.wegobackend.common.security.CustomUserDetails;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatRoomController implements ChatRoomControllerDocs {

    private final ChatRoomService chatRoomService;
    private final ChatMessageService chatMessageService;

    /**
     * 내 채팅방 목록 조회
     */
    @GetMapping("/rooms")
    public ResponseEntity<ApiResponse<ChatRoomListResponse>> getMyChatRooms(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        ChatRoomListResponse response = chatRoomService.getMyChatRooms(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), response));
    }

    /**
     * 채팅방 상세 조회
     */
    @GetMapping("/rooms/{roomId}")
    public ResponseEntity<ApiResponse<ChatRoomResponse>> getChatRoom(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long roomId
    ) {
        ChatRoomResponse response = chatRoomService.getChatRoom(userDetails.getId(), roomId);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), response));
    }

    /**
     * 메시지 이력 조회 (커서 기반 페이징)
     */
    @GetMapping("/rooms/{roomId}/messages")
    public ResponseEntity<ApiResponse<MessageListResponse>> getMessages(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long roomId,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "50") int size
    ) {
        MessageListResponse response = chatMessageService.getMessages(
                userDetails.getId(), roomId, cursor, size
        );
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), response));
    }

    /**
     * 읽음 처리
     */
    @PutMapping("/rooms/{roomId}/read")
    public ResponseEntity<ApiResponse<ReadStatusResponse>> markAsRead(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long roomId
    ) {
        ReadStatusResponse response = chatRoomService.markAsRead(userDetails.getId(), roomId);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), response));
    }

    /**
     * 참여자 추방 (방장 전용)
     */
    @PostMapping("/rooms/{roomId}/kick")
    public ResponseEntity<ApiResponse<Void>> kickParticipant(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long roomId,
            @RequestBody @Valid KickParticipantRequest request
    ) {
        chatRoomService.kickParticipant(userDetails.getId(), roomId, request.targetUserId());
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), null));
    }

    /**
     * 채팅방 나가기
     */
    @PostMapping("/rooms/{roomId}/leave")
    public ResponseEntity<ApiResponse<Void>> leaveChatRoom(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long roomId
    ) {
        chatRoomService.leaveChatRoom(userDetails.getId(), roomId);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), null));
    }

    /**
     * 1:1 채팅방 생성 또는 조회
     */
    @PostMapping("/dm")
    public ResponseEntity<ApiResponse<ChatRoomResponse>> createOrGetDmRoom(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid CreateDmRequest request
    ) {
        ChatRoomResponse response = chatRoomService.createOrGetDmRoom(
                userDetails.getId(), request.targetUserId()
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED.value(), response));
    }

    /**
     * 채팅방 참여자 목록 조회
     */
    @GetMapping("/rooms/{roomId}/participants")
    public ResponseEntity<ApiResponse<ParticipantListResponse>> getParticipants(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long roomId
    ) {
        ParticipantListResponse response = chatRoomService.getParticipants(
                userDetails.getId(), roomId
        );
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), response));
    }
}
