package team.wego.wegobackend.group.v2.application.event;

public record GroupJoinRejectedEvent(
        Long groupId,
        Long approverUserId,
        Long targetUserId
) {

}
