package team.wego.wegobackend.notification.application.dto.response;


import java.util.Map;
import team.wego.wegobackend.notification.application.dto.NotificationType;

public final class NotificationTypeMapper {
    private NotificationTypeMapper() {}

    private static final Map<NotificationType, String> MAP = Map.of(
            NotificationType.FOLLOW, "follow",
            NotificationType.GROUP_JOIN, "group-join",
            NotificationType.GROUP_LEAVE, "group-leave",
            NotificationType.GROUP_CREATE, "group-create",
            NotificationType.GROUP_DELETE, "group-delete",
            NotificationType.GROUP_JOIN_REQUEST, "group-join-request",
            NotificationType.GROUP_JOIN_APPROVED, "group-join-approved",
            NotificationType.GROUP_JOIN_REJECTED, "group-join-rejected",
            NotificationType.GROUP_JOIN_KICKED, "group-join-kicked"
    );

    public static String toClientType(NotificationType type) {
        return MAP.getOrDefault(type, type.name().toLowerCase());
    }
}
