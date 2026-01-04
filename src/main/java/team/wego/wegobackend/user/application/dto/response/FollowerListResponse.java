package team.wego.wegobackend.user.application.dto.response;

import java.util.List;

public record FollowerListResponse(List<WrapperFollowerResponse> items, Long nextCursor) {

}
