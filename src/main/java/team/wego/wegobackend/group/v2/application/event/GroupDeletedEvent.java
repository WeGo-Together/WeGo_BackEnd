package team.wego.wegobackend.group.v2.application.event;

import java.util.List;

public record GroupDeletedEvent(
        Long groupId,
        Long hostId,
        List<Long> attendeeUserIds
) {

}
