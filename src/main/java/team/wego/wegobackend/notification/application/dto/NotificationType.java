package team.wego.wegobackend.notification.application.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

public enum NotificationType {

    TEST("test", "테스트"),
    // user
    FOLLOW("user", "A가 B를 팔로우"),

    // group
    GROUP_JOIN("group", "모임 참여"),
    GROUP_LEAVE("group", "모임 탈퇴"),
    GROUP_CREATE("group", "모임 생성"),
    GROUP_DELETE("group", "모임 삭제"),
    GROUP_JOIN_REQUEST("group", "모임 참여 신청"),
    GROUP_JOIN_APPROVED("group", "모임 참여 승인"),
    GROUP_JOIN_REJECTED("group", "모임 참여 거절"),
    GROUP_JOIN_KICKED("group", "모임 강퇴");

    private final String domain;
    private final String description;

    NotificationType(String domain, String description) {
        this.domain = domain;
        this.description = description;
    }

    public String domain() { return domain; }
    public String description() { return description; }
}
