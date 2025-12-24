package team.wego.wegobackend.group.v2.application.event;

public record GroupJoinKickedEvent(
        Long groupId,
        Long hostId,
        Long targetUserId
) {

}

