package team.wego.wegobackend.auth.exception;

import team.wego.wegobackend.common.exception.AppErrorCode;
import team.wego.wegobackend.common.exception.AppException;

public class InvalidPasswordException extends AppException {

    public InvalidPasswordException() {
        super(AppErrorCode.INVALID_PASSWORD_VALUE);
    }
}
