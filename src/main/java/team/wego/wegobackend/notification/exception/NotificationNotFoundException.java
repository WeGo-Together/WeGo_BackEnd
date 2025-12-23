package team.wego.wegobackend.notification.exception;

import team.wego.wegobackend.common.exception.AppErrorCode;
import team.wego.wegobackend.common.exception.AppException;

public class NotificationNotFoundException extends AppException {

    public NotificationNotFoundException() {
        super(AppErrorCode.NOT_FOUND_NOTIFICATION);
    }
}
