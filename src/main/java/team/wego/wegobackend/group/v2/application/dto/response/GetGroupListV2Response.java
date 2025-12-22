package team.wego.wegobackend.group.v2.application.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import team.wego.wegobackend.group.v2.domain.entity.GroupV2JoinPolicy;
import team.wego.wegobackend.group.v2.domain.entity.GroupV2Status;

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
            GroupV2JoinPolicy joinPolicy,

            GroupV2Status status, //  뱃지/상태 표시용

            String location,
            String locationDetail,

            LocalDateTime startTime,
            LocalDateTime endTime,

            List<String> images,
            List<String> tags,

            String description,

            int participantCount,
            int maxParticipants,

            int remainingSeats, //  남은 자리(프론트 계산 제거)
            boolean joinable,   //  참여 버튼 활성화 여부

            CreatedByV2Response createdBy,

            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {

        public static GroupListItemV2Response of(
                Long id,
                String title,
                GroupV2JoinPolicy joinPolicy,
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
                CreatedByV2Response createdBy,
                LocalDateTime createdAt,
                LocalDateTime updatedAt
        ) {
            int remainingSeats = Math.max(0, maxParticipants - participantCount);

            //RECRUITING + 남은 자리 > 0:  참여 가능
            //FULL/CLOSED/CANCELLED/FINISHED: 참여 불가
            boolean joinable = switch (status) {
                case RECRUITING -> remainingSeats > 0;
                case FULL, CLOSED, CANCELLED, FINISHED -> false;
            };

            return new GroupListItemV2Response(
                    id,
                    title,
                    joinPolicy,
                    status,
                    location,
                    locationDetail,
                    startTime,
                    endTime,
                    images,
                    tags,
                    description,
                    participantCount,
                    maxParticipants,
                    remainingSeats,
                    joinable,
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

            public static CreatedByV2Response of(Long userId, String nickName,
                    String profileImage) {
                return new CreatedByV2Response(userId, nickName, profileImage);
            }
        }
    }
}
