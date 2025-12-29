package team.wego.wegobackend.group.v2.application.dto.common;

import java.time.LocalDateTime;
import team.wego.wegobackend.group.v2.domain.entity.GroupUserV2Status;

public record JoinRequestItem(
        Long userId,
        String nickName,
        String profileImage,
        Long groupUserId,
        GroupUserV2Status status,
        LocalDateTime joinedAt,
        String joinRequestMessage
) {

}


