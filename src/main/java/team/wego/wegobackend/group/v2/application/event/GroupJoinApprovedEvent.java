package team.wego.wegobackend.group.v2.application.event;

public record GroupJoinApprovedEvent(
        Long groupId,
        Long approverUserId,
        Long targetUserId
) {

}

