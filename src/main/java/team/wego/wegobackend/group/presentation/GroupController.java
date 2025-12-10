package team.wego.wegobackend.group.presentation;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import team.wego.wegobackend.common.response.ApiResponse;
import team.wego.wegobackend.group.application.dto.request.CreateGroupRequest;
import team.wego.wegobackend.group.application.dto.request.UpdateGroupRequest;
import team.wego.wegobackend.group.application.dto.response.CreateGroupResponse;
import team.wego.wegobackend.group.application.dto.response.GetGroupListResponse;
import team.wego.wegobackend.group.application.dto.response.GetGroupResponse;
import team.wego.wegobackend.group.application.service.GroupService;

@RequiredArgsConstructor
@RequestMapping("/api/v1/groups")
@RestController
public class GroupController {

    private final GroupService groupService;

    // TODO: 유저 정보 파싱 전, 임시 userId
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<CreateGroupResponse>> createGroupResponse(
            @RequestParam Long userId,
            @RequestBody @Valid CreateGroupRequest request
    ) {
        CreateGroupResponse response = groupService.createGroup(userId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        HttpStatus.CREATED.value(),
                        response));
    }

    @PostMapping("/{groupId}/attend")
    public ResponseEntity<ApiResponse<GetGroupResponse>> attendGroup(
            @PathVariable Long groupId,
            @RequestParam Long userId
    ) {
        GetGroupResponse response = groupService.attendGroup(groupId, userId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(
                        HttpStatus.OK.value(), response));
    }


    @PostMapping("/{groupId}/cancel")
    public ResponseEntity<ApiResponse<GetGroupResponse>> cancelAttendGroup(
            @PathVariable Long groupId,
            @RequestParam Long userId   // TODO: 나중에 인증 정보에서 꺼내기
    ) {
        GetGroupResponse response = groupService.cancelAttendGroup(groupId, userId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(HttpStatus.OK.value(), response));
    }

    @GetMapping("/{groupId}")
    public ResponseEntity<ApiResponse<GetGroupResponse>> getGroupResponse(
            @PathVariable Long groupId,
            @RequestParam(required = false) Long userId // TODO: 나중에 인증에서 꺼내기
    ) {
        GetGroupResponse response = (userId == null)
                ? groupService.getGroup(groupId)
                : groupService.getGroup(groupId, userId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(HttpStatus.OK.value(), response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<GetGroupListResponse>> getGroupList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long cursor,
            @RequestParam int size
    ) {
        GetGroupListResponse response = groupService.getGroupList(keyword, cursor, size);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(HttpStatus.OK.value(), response));
    }

    @PatchMapping("/{groupId}")
    public ResponseEntity<ApiResponse<GetGroupResponse>> updateGroup(
            @RequestParam Long userId,
            @PathVariable Long groupId,
            @RequestBody @Valid UpdateGroupRequest request
    ) {
        GetGroupResponse response = groupService.updateGroup(userId, groupId, request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(HttpStatus.OK.value(), response));
    }

    // 모임 삭제
    @DeleteMapping("/{groupId}")
    public ResponseEntity<ApiResponse<Void>> deleteGroup(
            @RequestParam Long userId,  // TODO: 나중에 인증 정보에서 꺼내기
            @PathVariable Long groupId
    ) {
        groupService.deleteGroup(userId, groupId);

        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .body(ApiResponse.success(HttpStatus.NO_CONTENT.value(), null));
    }


    @GetMapping("/me")
    public ResponseEntity<ApiResponse<GetGroupListResponse>> getMyGroups(
            @RequestParam Long userId,        // TODO: 나중에 인증 정보에서 꺼내기
            @RequestParam String type,
            @RequestParam(required = false) Long cursor,
            @RequestParam int size
    ) {
        GetGroupListResponse response =
                groupService.getMyGroups(userId, type, cursor, size);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(HttpStatus.OK.value(), response));
    }

}
