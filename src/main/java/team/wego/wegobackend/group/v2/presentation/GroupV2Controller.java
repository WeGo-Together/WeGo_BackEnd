package team.wego.wegobackend.group.v2.presentation;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import team.wego.wegobackend.common.response.ApiResponse;
import team.wego.wegobackend.common.security.CustomUserDetails;
import team.wego.wegobackend.group.application.dto.v1.response.GetGroupResponse;
import team.wego.wegobackend.group.v2.application.dto.request.CreateGroupV2Request;
import team.wego.wegobackend.group.v2.application.dto.response.AttendGroupV2Response;
import team.wego.wegobackend.group.v2.application.dto.response.CreateGroupV2Response;
import team.wego.wegobackend.group.v2.application.dto.response.GetGroupListV2Response;
import team.wego.wegobackend.group.v2.application.dto.response.GetGroupV2Response;
import team.wego.wegobackend.group.v2.application.service.GroupV2Service;

@RequiredArgsConstructor
@RequestMapping("/api/v2/groups")
@RestController
public class GroupV2Controller {

    private final GroupV2Service groupV2Service;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<CreateGroupV2Response>> createGroup(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid CreateGroupV2Request request
    ) {
        CreateGroupV2Response response = groupV2Service.create(userDetails.getId(), request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        HttpStatus.CREATED.value(),
                        response));
    }

    @GetMapping("/{groupId}")
    public ResponseEntity<ApiResponse<GetGroupV2Response>> getGroup(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long groupId
    ) {
        Long userId = (userDetails == null) ? null : userDetails.getId();
        GetGroupV2Response response = groupV2Service.getGroup(userId, groupId);

        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), response));
    }

    @PostMapping("/{groupId}/attend")
    public ResponseEntity<ApiResponse<AttendGroupV2Response>> attend(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long groupId
    ) {

        AttendGroupV2Response response = groupV2Service.attend(userDetails.getId(), groupId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(
                        HttpStatus.OK.value(), response));
    }


    @PostMapping("/{groupId}/left")
    public ResponseEntity<ApiResponse<AttendGroupV2Response>> left(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long groupId
    ) {
        AttendGroupV2Response response = groupV2Service.left(userDetails.getId(), groupId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(HttpStatus.OK.value(), response));
    }


    @GetMapping
    public ResponseEntity<ApiResponse<GetGroupListV2Response>> getGroupList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long cursor,
            @RequestParam int size
    ) {
        GetGroupListV2Response response = groupV2Service.getGroupListV2(keyword, cursor, size);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(HttpStatus.OK.value(), response));
    }


}
