package team.wego.wegobackend.group.v2.application.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import team.wego.wegobackend.group.v2.application.dto.common.CreatedBy;
import team.wego.wegobackend.group.v2.application.dto.common.MyMembership;
import team.wego.wegobackend.group.v2.domain.entity.GroupV2Status;

public record GetMyGroupListV2Response(
        List<Item> items,
        Long nextCursor
) {

    public static GetMyGroupListV2Response of(List<Item> items, Long nextCursor) {
        return new GetMyGroupListV2Response(items, nextCursor);
    }

    public record Item(
            Long id,
            String title,
            GroupV2Status status,
            String location,
            String locationDetail,
            LocalDateTime startTime,
            LocalDateTime endTime,
            List<String> images,
            List<String> tags,
            String description,
            int participantCount,
            int maxParticipants,
            int remainingSeats,
            boolean joinable,
            CreatedBy createdBy,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            MyMembership myMembership
    ) {

        public static Item of(
                Long id,
                String title,
                GroupV2Status status,
                String location,
                String locationDetail,
                LocalDateTime startTime,
                LocalDateTime endTime,
                List<String> images,
                List<String> tags,
                String description,
                int participantCount,
                int maxParticipants,
                CreatedBy createdBy,
                LocalDateTime createdAt,
                LocalDateTime updatedAt,
                MyMembership myMembership
        ) {
            int remainingSeats = Math.max(0, maxParticipants - participantCount);
            boolean joinable = switch (status) {
                case RECRUITING -> remainingSeats > 0;
                case FULL, CLOSED, CANCELLED, FINISHED -> false;
            };
            return new Item(
                    id, title, status, location, locationDetail,
                    startTime, endTime, images, tags, description,
                    participantCount, maxParticipants, remainingSeats, joinable,
                    createdBy, createdAt, updatedAt, myMembership
            );
        }
    }

}