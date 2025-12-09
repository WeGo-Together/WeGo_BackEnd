package team.wego.wegobackend.user.exception;

import team.wego.wegobackend.common.exception.AppErrorCode;
import team.wego.wegobackend.common.exception.AppException;

public class UserNotFoundException extends AppException {

    public UserNotFoundException() {
        super(AppErrorCode.USER_NOT_FOUND);
    }
}
