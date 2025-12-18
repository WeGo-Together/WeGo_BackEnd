package team.wego.wegobackend.group.v2.application.dto.request;

import java.util.List;
import team.wego.wegobackend.group.v2.domain.entity.GroupV2Status;

public enum GroupListFilter {
    ACTIVE,     // 기본: 현재 사용자에게 노출되는 목록
    ARCHIVED,   // 종료/취소 등 아카이브
    ALL;        // 전부

    public List<GroupV2Status> defaultIncludeStatuses() {
        return switch (this) {
            case ACTIVE -> List.of(GroupV2Status.RECRUITING, GroupV2Status.FULL, GroupV2Status.CLOSED);
            case ARCHIVED -> List.of(GroupV2Status.CANCELLED, GroupV2Status.FINISHED);
            case ALL -> List.of(); // 빈 리스트 = 전체(= include 조건 미적용)
        };
    }

    public List<GroupV2Status> defaultExcludeStatuses() {
        return List.of(); // 기본 non-null
    }
}