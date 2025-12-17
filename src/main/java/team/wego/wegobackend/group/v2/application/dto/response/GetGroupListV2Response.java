package team.wego.wegobackend.group.v2.application.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record GetGroupListV2Response(
        List<GroupListItemV2Response> items,
        Long nextCursor
) {
    public static GetGroupListV2Response of(List<GroupListItemV2Response> items, Long nextCursor) {
        return new GetGroupListV2Response(items, nextCursor);
    }

    public record GroupListItemV2Response(
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
            CreatedByV2Response createdBy,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {

        public static GroupListItemV2Response of(
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
                CreatedByV2Response createdBy,
                LocalDateTime createdAt,
                LocalDateTime updatedAt
        ) {
            return new GroupListItemV2Response(
                    id,
                    title,
                    location,
                    locationDetail,
                    startTime,
                    endTime,
                    images,
                    tags,
                    description,
                    participantCount,
                    maxParticipants,
                    createdBy,
                    createdAt,
                    updatedAt
            );
        }

        public record CreatedByV2Response(
                Long userId,
                String nickName,
                String profileImage
        ) {
            public static CreatedByV2Response of(Long userId, String nickName, String profileImage) {
                return new CreatedByV2Response(userId, nickName, profileImage);
            }
        }
    }
}

