package team.wego.wegobackend.group.v2.infrastructure.querydsl.projection;

import java.time.LocalDateTime;

public record GroupListRow(
        Long groupId,
        String title,
        String location,
        String locationDetail,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String description,
        int maxParticipants,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Long hostId,
        String hostNickName,
        String hostProfileImage,
        Long participantCount
) {}

