package team.wego.wegobackend.group.v2.infrastructure.querydsl.projection;

import java.time.LocalDateTime;
import team.wego.wegobackend.group.v2.domain.entity.GroupUserV2Role;
import team.wego.wegobackend.group.v2.domain.entity.GroupUserV2Status;
import team.wego.wegobackend.group.v2.domain.entity.GroupV2Status;

public record MyGroupListRow(
        Long groupId,
        String title,
        GroupV2Status status,
        String location,
        String locationDetail,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String description,
        Integer maxParticipants,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,

        Long hostId,
        String hostNickName,
        String hostProfileImage,
        String hostProfileMessage,

        Long participantCount,

        Long myGroupUserId,
        GroupUserV2Role myRole,
        GroupUserV2Status myStatus,
        LocalDateTime myJoinedAt,
        LocalDateTime myLeftAt
) {}
