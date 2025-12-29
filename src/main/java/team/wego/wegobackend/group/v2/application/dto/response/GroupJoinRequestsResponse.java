package team.wego.wegobackend.group.v2.application.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import team.wego.wegobackend.group.v2.application.dto.common.JoinRequestItem;
import team.wego.wegobackend.group.v2.domain.entity.GroupUserV2Status;

public record GroupJoinRequestsResponse(
        Long groupId,
        GroupUserV2Status status,
        int count,
        List<JoinRequestItem> items,
        LocalDateTime serverTime
) {
    public static GroupJoinRequestsResponse of(
            Long groupId,
            GroupUserV2Status status,
            List<JoinRequestItem> items
    ) {
        return new GroupJoinRequestsResponse(
                groupId,
                status,
                items == null ? 0 : items.size(),
                items,
                LocalDateTime.now()
        );
    }
}
