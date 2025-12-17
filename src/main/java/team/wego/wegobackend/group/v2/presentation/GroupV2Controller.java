package team.wego.wegobackend.group.presentation.v2;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import team.wego.wegobackend.common.response.ApiResponse;
import team.wego.wegobackend.group.application.dto.v2.response.GetGroupListV2Response;
import team.wego.wegobackend.group.application.service.v2.GroupV2Service;

@RequiredArgsConstructor
@RequestMapping("/api/v2/groups")
@RestController
public class GroupV2Controller {

    private final GroupV2Service groupV2Service;

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
