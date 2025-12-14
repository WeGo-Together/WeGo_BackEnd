package team.wego.wegobackend.group.application.dto.v1.response;

import java.util.List;

public record GetGroupListResponse(
        List<GroupListItemResponse> items,
        Long nextCursor
) {

    public static GetGroupListResponse of(List<GroupListItemResponse> items, Long nextCursor) {
        return new GetGroupListResponse(items, nextCursor);
    }
}

