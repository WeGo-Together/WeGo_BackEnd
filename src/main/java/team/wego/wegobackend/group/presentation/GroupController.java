package team.wego.wegobackend.group.presentation;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
import team.wego.wegobackend.common.security.CustomUserDetails;
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

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<CreateGroupResponse>> createGroupResponse(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid CreateGroupRequest request
    ) {
        CreateGroupResponse response = groupService.createGroup(userDetails, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        HttpStatus.CREATED.value(),
                        response));
    }

    @PostMapping("/{groupId}/attend")
    public ResponseEntity<ApiResponse<GetGroupResponse>> attendGroup(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long groupId
    ) {
        GetGroupResponse response = groupService.attendGroup(userDetails, groupId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(
                        HttpStatus.OK.value(), response));
    }


    @PostMapping("/{groupId}/cancel")
    public ResponseEntity<ApiResponse<GetGroupResponse>> cancelAttendGroup(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long groupId
    ) {
        GetGroupResponse response = groupService.cancelAttendGroup(userDetails, groupId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(HttpStatus.OK.value(), response));
    }

    @GetMapping("/{groupId}")
    public ResponseEntity<ApiResponse<GetGroupResponse>> getGroupResponse(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long groupId
    ) {
        GetGroupResponse response = (userDetails == null)
                ? groupService.getGroup(groupId)
                : groupService.getGroup(userDetails, groupId);

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
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long groupId,
            @RequestBody @Valid UpdateGroupRequest request
    ) {
        GetGroupResponse response = groupService.updateGroup(userDetails, groupId, request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(HttpStatus.OK.value(), response));
    }

    @DeleteMapping("/{groupId}")
    public ResponseEntity<ApiResponse<Void>> deleteGroup(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long groupId
    ) {
        groupService.deleteGroup(userDetails, groupId);

        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .body(ApiResponse.success(HttpStatus.NO_CONTENT.value(), null));
    }


    @GetMapping("/me")
    public ResponseEntity<ApiResponse<GetGroupListResponse>> getMyGroups(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam String type,
            @RequestParam(required = false) Long cursor,
            @RequestParam int size
    ) {
        GetGroupListResponse response =
                groupService.getMyGroups(userDetails, type, cursor, size);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(HttpStatus.OK.value(), response));
    }
}


