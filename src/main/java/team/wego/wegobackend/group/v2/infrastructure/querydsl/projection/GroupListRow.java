package team.wego.wegobackend.group.v2.infrastructure.querydsl.projection;

import java.time.LocalDateTime;
import team.wego.wegobackend.group.v2.domain.entity.GroupV2JoinPolicy;
import team.wego.wegobackend.group.v2.domain.entity.GroupV2Status;

public record GroupListRow(
        Long groupId,
        String title,
        GroupV2JoinPolicy joinPolicy,
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
        Long participantCount
) {}

