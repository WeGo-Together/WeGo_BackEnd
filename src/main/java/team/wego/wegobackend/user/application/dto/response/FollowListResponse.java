package team.wego.wegobackend.user.application.dto.response;

import java.util.List;

public record FollowListResponse(List<FollowResponse> items, Long nextCursor) {

}
