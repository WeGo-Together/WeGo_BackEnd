package team.wego.wegobackend.group.v2.application.event;

public record GroupCreatedEvent(
        Long groupId,
        Long hostUserId
) {

}
