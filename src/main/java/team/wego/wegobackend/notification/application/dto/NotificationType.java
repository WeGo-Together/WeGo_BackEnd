package team.wego.wegobackend.notification.application.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationType {
    FOLLOW("팔로우"),
    ENTER("모임 참여"),
    EXIT("모임 퇴장"),
    CREATE("모임 생성"),
    CANCLE("모임 취소")
    ;

    private final String description;
}
