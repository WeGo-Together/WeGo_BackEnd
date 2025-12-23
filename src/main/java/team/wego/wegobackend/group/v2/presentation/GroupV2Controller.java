package team.wego.wegobackend.group.v2.presentation;

import jakarta.validation.Valid;
import java.util.List;
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
import team.wego.wegobackend.group.v2.application.dto.request.CreateGroupV2Request;
import team.wego.wegobackend.group.v2.application.dto.request.GroupListFilter;
import team.wego.wegobackend.group.v2.application.dto.request.MyGroupTypeV2;
import team.wego.wegobackend.group.v2.application.dto.request.UpdateGroupV2Request;
import team.wego.wegobackend.group.v2.application.dto.response.AttendanceGroupV2Response;
import team.wego.wegobackend.group.v2.application.dto.response.CreateGroupV2Response;
import team.wego.wegobackend.group.v2.application.dto.response.GetBanTargetsResponse;
import team.wego.wegobackend.group.v2.application.dto.response.GetBannedTargetsResponse;
import team.wego.wegobackend.group.v2.application.dto.response.GetGroupListV2Response;
import team.wego.wegobackend.group.v2.application.dto.response.GetGroupV2Response;
import team.wego.wegobackend.group.v2.application.dto.response.GetKickTargetsResponse;
import team.wego.wegobackend.group.v2.application.dto.response.GetMyGroupListV2Response;
import team.wego.wegobackend.group.v2.application.dto.response.GroupUserV2StatusResponse;
import team.wego.wegobackend.group.v2.application.dto.response.UpdateGroupV2Response;
import team.wego.wegobackend.group.v2.application.service.GroupMyGetV2Service;
import team.wego.wegobackend.group.v2.application.service.GroupV2AttendanceService;
import team.wego.wegobackend.group.v2.application.service.GroupV2DeleteService;
import team.wego.wegobackend.group.v2.application.service.GroupV2Service;
import team.wego.wegobackend.group.v2.application.service.GroupV2UpdateService;
import team.wego.wegobackend.group.v2.domain.entity.GroupUserV2Status;
import team.wego.wegobackend.group.v2.domain.entity.GroupV2Status;

@RequiredArgsConstructor
@RequestMapping("/api/v2/groups")
@RestController
public class GroupV2Controller implements GroupV2ControllerDocs {

    private final GroupV2Service groupV2Service;
    private final GroupV2UpdateService groupV2UpdateService;
    private final GroupMyGetV2Service groupMyGetV2Service;
    private final GroupV2DeleteService groupV2DeleteService;
    private final GroupV2AttendanceService groupV2AttendanceService;


    @PostMapping("/create")
    public ResponseEntity<ApiResponse<CreateGroupV2Response>> create(
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
        Long userIdOrNull = (userDetails == null) ? null : userDetails.getId();
        GetGroupV2Response response = groupV2Service.getGroup(userIdOrNull, groupId);

        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), response));
    }

    @PostMapping("/{groupId}/attend")
    public ResponseEntity<ApiResponse<AttendanceGroupV2Response>> attend(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long groupId
    ) {

        AttendanceGroupV2Response response = groupV2AttendanceService.attend(userDetails.getId(),
                groupId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(
                        HttpStatus.OK.value(), response));
    }


    @PostMapping("/{groupId}/left")
    public ResponseEntity<ApiResponse<AttendanceGroupV2Response>> left(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long groupId
    ) {
        AttendanceGroupV2Response response = groupV2AttendanceService.left(userDetails.getId(),
                groupId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(HttpStatus.OK.value(), response));
    }


    @GetMapping
    public ResponseEntity<ApiResponse<GetGroupListV2Response>> getGroupList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "ACTIVE") GroupListFilter filter,
            @RequestParam(required = false) List<GroupV2Status> includeStatuses,
            @RequestParam(required = false) List<GroupV2Status> excludeStatuses
    ) {
        GetGroupListV2Response response =
                groupV2Service.getGroupListV2(keyword, cursor, size, filter, includeStatuses,
                        excludeStatuses);

        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), response));
    }


    @PatchMapping("/{groupId}")
    public ResponseEntity<ApiResponse<UpdateGroupV2Response>> update(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long groupId,
            @Valid @RequestBody UpdateGroupV2Request request
    ) {
        UpdateGroupV2Response response =
                groupV2UpdateService.update(userDetails.getId(), groupId, request);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), response));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<GetMyGroupListV2Response>> getMyGroups(
            @AuthenticationPrincipal CustomUserDetails userDetails,

            @RequestParam(required = false) String type,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "20") int size,

            @RequestParam(required = false) GroupListFilter filter,
            @RequestParam(required = false) List<GroupV2Status> includeStatuses,
            @RequestParam(required = false) List<GroupV2Status> excludeStatuses,

            @RequestParam(required = false) List<GroupUserV2Status> myStatuses
    ) {
        MyGroupTypeV2 myType = (type == null) ? MyGroupTypeV2.CURRENT : MyGroupTypeV2.from(type);

        GetMyGroupListV2Response response = groupMyGetV2Service.getMyGroups(
                userDetails.getId(),
                cursor,
                size,
                myType,
                filter,
                includeStatuses,
                excludeStatuses,
                myStatuses
        );

        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), response));
    }


    @DeleteMapping("/{groupId}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long groupId) {
        groupV2DeleteService.deleteHard(userDetails.getId(), groupId);

        return ResponseEntity.noContent().build();
    }


    @PostMapping("/{groupId}/attendance/{targetUserId}/approve")
    public ResponseEntity<ApiResponse<GroupUserV2StatusResponse>> approve(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long groupId,
            @PathVariable Long targetUserId
    ) {
        GroupUserV2StatusResponse response =
                groupV2AttendanceService.approve(userDetails.getId(), groupId, targetUserId);

        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), response));
    }

    @PostMapping("/{groupId}/attendance/{targetUserId}/reject")
    public ResponseEntity<ApiResponse<GroupUserV2StatusResponse>> reject(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long groupId,
            @PathVariable Long targetUserId
    ) {
        GroupUserV2StatusResponse response =
                groupV2AttendanceService.reject(userDetails.getId(), groupId, targetUserId);

        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), response));
    }

    @PostMapping("/{groupId}/attendance/{targetUserId}/kick")
    public ResponseEntity<ApiResponse<GroupUserV2StatusResponse>> kick(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long groupId,
            @PathVariable Long targetUserId
    ) {
        GroupUserV2StatusResponse response =
                groupV2AttendanceService.kick(userDetails.getId(), groupId, targetUserId);

        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), response));
    }

    @PostMapping("/{groupId}/attendance/{targetUserId}/ban")
    public ResponseEntity<ApiResponse<GroupUserV2StatusResponse>> ban(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long groupId,
            @PathVariable Long targetUserId
    ) {
        GroupUserV2StatusResponse response =
                groupV2AttendanceService.ban(userDetails.getId(), groupId, targetUserId);

        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), response));
    }

    @PostMapping("/{groupId}/attendance/{targetUserId}/unban")
    public ResponseEntity<ApiResponse<GroupUserV2StatusResponse>> unban(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long groupId,
            @PathVariable Long targetUserId
    ) {
        GroupUserV2StatusResponse response =
                groupV2AttendanceService.unban(userDetails.getId(), groupId, targetUserId);

        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), response));
    }

    @GetMapping("/{groupId}/attendance/kick-targets")
    public ResponseEntity<ApiResponse<GetKickTargetsResponse>> getKickTargets(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long groupId
    ) {
        GetKickTargetsResponse response =
                groupV2AttendanceService.getKickTargets(userDetails.getId(), groupId);

        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), response));
    }

    @GetMapping("/{groupId}/attendance/ban-targets")
    public ResponseEntity<ApiResponse<GetBanTargetsResponse>> getBanTargets(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long groupId
    ) {
        GetBanTargetsResponse response =
                groupV2AttendanceService.getBanTargets(userDetails.getId(), groupId);

        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), response));
    }

    @GetMapping("/{groupId}/attendance/banned-targets")
    public ResponseEntity<ApiResponse<GetBannedTargetsResponse>> getBannedTargets(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long groupId
    ) {
        GetBannedTargetsResponse response =
                groupV2AttendanceService.getBannedTargets(userDetails.getId(), groupId);

        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), response));
    }
}

