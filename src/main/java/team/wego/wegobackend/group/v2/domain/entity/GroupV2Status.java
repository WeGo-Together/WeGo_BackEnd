package team.wego.wegobackend.group.v2.domain.entity;

import java.util.EnumSet;
import java.util.Set;

public enum GroupV2Status {

    RECRUITING,   // 모집중 (기본값)
    FULL,         // 정원마감 (자동/수동 모두 가능)
    CLOSED,       // 모집마감 (더 이상 참가 불가)
    CANCELLED,    // 모임취소
    FINISHED;     // 모임종료 (시간 경과 후 처리 or 수동)

    public boolean canTransitionTo(GroupV2Status status) {
        return allowedNext(this).contains(status);
    }

    private static Set<GroupV2Status> allowedNext(GroupV2Status current) {
        return switch (current) {
            case RECRUITING -> EnumSet.of(FULL, CLOSED, CANCELLED, FINISHED);
            case FULL       -> EnumSet.of(RECRUITING, CLOSED, CANCELLED, FINISHED);
            case CLOSED     -> EnumSet.of(CANCELLED, FINISHED);
            case CANCELLED, FINISHED -> EnumSet.noneOf(GroupV2Status.class);
        };
    }
}
