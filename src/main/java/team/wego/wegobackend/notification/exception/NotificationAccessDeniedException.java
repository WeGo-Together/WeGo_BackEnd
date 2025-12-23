package team.wego.wegobackend.notification.exception;

import team.wego.wegobackend.common.exception.AppErrorCode;
import team.wego.wegobackend.common.exception.AppException;

public class NotificationAccessDeniedException extends AppException {

    public NotificationAccessDeniedException() {
        super(AppErrorCode.USER_ACCESS_DENIED);
    }
}
