package team.wego.wegobackend.group.application.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import team.wego.wegobackend.group.domain.entity.Group;

public record GroupListItemResponse(
        Long id,
        String title,
        String location,
        String locationDetail,
        LocalDateTime startTime,
        LocalDateTime endTime,
        List<String> images,
        List<String> tags,
        String description,
        int participantCount,
        int maxParticipants,
        GetGroupResponse.CreatedByResponse createdBy,
        LocalDateTime createdAt,
        LocalDateTime updatedAt

) {

    public static GroupListItemResponse of(
            Group group,
            List<String> imageUrls,
            List<String> tagNames,
            int participantCount,
            GetGroupResponse.CreatedByResponse createdBy
    ) {
        return new GroupListItemResponse(
                group.getId(),
                group.getTitle(),
                group.getLocation(),
                group.getLocationDetail(),
                group.getStartTime(),
                group.getEndTime(),
                imageUrls,
                tagNames,
                group.getDescription(),
                participantCount,
                group.getMaxParticipants(),
                createdBy,
                group.getCreatedAt(),
                group.getUpdatedAt()

        );
    }
}
