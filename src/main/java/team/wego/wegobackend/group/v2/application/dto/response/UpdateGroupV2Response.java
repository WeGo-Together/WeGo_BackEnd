package team.wego.wegobackend.group.v2.application.dto.response;


import java.time.LocalDateTime;
import java.util.List;
import team.wego.wegobackend.group.v2.application.dto.common.Address;
import team.wego.wegobackend.group.v2.application.dto.common.GroupImageItem;
import team.wego.wegobackend.group.v2.domain.entity.GroupV2JoinPolicy;
import team.wego.wegobackend.group.v2.domain.entity.GroupV2Status;

public record UpdateGroupV2Response(
        Long id,
        String title,
        GroupV2JoinPolicy joinPolicy,
        GroupV2Status status,
        Address address,
        LocalDateTime startTime,
        LocalDateTime endTime,
        List<GroupImageItem> images,
        List<String> tags,
        String description,
        int maxParticipants,
        LocalDateTime updatedAt
) {

}


