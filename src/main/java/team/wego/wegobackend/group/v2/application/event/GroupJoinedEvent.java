package team.wego.wegobackend.group.v2.application.event;

public record GroupJoinedEvent(
        Long groupId,
        Long hostId,
        Long joinerUserId
) {

}

