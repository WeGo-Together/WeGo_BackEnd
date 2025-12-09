package team.wego.wegobackend.group.presentation;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import team.wego.wegobackend.common.response.ApiResponse;
import team.wego.wegobackend.group.application.dto.request.CreateGroupRequest;
import team.wego.wegobackend.group.application.dto.response.CreateGroupResponse;
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
                .body(ApiResponse.success(response));
    }

    // 모임 참여
    @PostMapping("/{groupId}/attend")
    public ResponseEntity<ApiResponse<GetGroupResponse>> attendGroup(
            @PathVariable Long groupId,
            @RequestParam Long userId
    ) {
        GetGroupResponse response = groupService.attendGroup(groupId, userId);

        return  ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(response));
    }

    // 모임 참여 취소

    // 모임 상세 조회
    @PostMapping("/{groupId}")
    public ResponseEntity<ApiResponse<GetGroupResponse>> getGroupResponse(
            @PathVariable Long groupId
    ) {
//        GetGroupResponse response = groupService.getGroup(groupId);

        return ResponseEntity.noContent().build();
    }

    // 모임 목록 조회(무한스크롤, 커서 기반)

    // 모임 수정

    // 나의 모임 목록 조회

    //
}
