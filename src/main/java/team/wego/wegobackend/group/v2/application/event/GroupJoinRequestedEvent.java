package team.wego.wegobackend.group.v2.application.event;

public record GroupJoinRequestedEvent(
        Long groupId,
        Long hostUserId,
        Long requesterUserId
) {

}



