package team.wego.wegobackend.notification.application.dto.response;


import java.util.Map;
import team.wego.wegobackend.notification.application.dto.NotificationType;

public final class NotificationTypeMapper {
    private NotificationTypeMapper() {}

    private static final Map<NotificationType, String> MAP = Map.of(
            NotificationType.FOLLOW, "FOLLOW",
            NotificationType.GROUP_JOIN, "GROUP_JOIN",
            NotificationType.GROUP_LEAVE, "GROUP_LEAVE",
            NotificationType.GROUP_CREATE, "GROUP_CREATE",
            NotificationType.GROUP_DELETE, "GROUP_DELETE",
            NotificationType.GROUP_JOIN_REQUEST, "GROUP_JOIN_REQUEST",
            NotificationType.GROUP_JOIN_APPROVED, "GROUP_JOIN_APPROVED",
            NotificationType.GROUP_JOIN_REJECTED, "GROUP_JOIN_REJECTED",
            NotificationType.GROUP_JOIN_KICKED, "GROUP_JOIN_KICKED"
    );

    public static String toClientType(NotificationType type) {
        return MAP.getOrDefault(type, type.name().toLowerCase());
    }
}
