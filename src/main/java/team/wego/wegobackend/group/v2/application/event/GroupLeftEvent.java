package team.wego.wegobackend.group.v2.application.event;

public record GroupLeftEvent(
        Long groupId,
        Long hostId,
        Long leaverUserId
) {

}

