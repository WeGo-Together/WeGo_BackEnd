package team.wego.wegobackend.auth.exception;

import team.wego.wegobackend.common.exception.AppErrorCode;
import team.wego.wegobackend.common.exception.AppException;

public class UserAlreadyExistsException extends AppException {

    public UserAlreadyExistsException() {
        super(AppErrorCode.ALREADY_EXISTS_USER);
    }
}
