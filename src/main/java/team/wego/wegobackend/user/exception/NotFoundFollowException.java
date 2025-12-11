package team.wego.wegobackend.user.exception;

import team.wego.wegobackend.common.exception.AppErrorCode;
import team.wego.wegobackend.common.exception.AppException;

public class NotFoundFollowException extends AppException {

    public NotFoundFollowException() {
        super(AppErrorCode.NOT_FOUND_FOLLOW);
    }
}
