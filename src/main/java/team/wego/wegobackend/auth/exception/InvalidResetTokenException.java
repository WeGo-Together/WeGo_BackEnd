package team.wego.wegobackend.auth.exception;

import team.wego.wegobackend.common.exception.AppErrorCode;
import team.wego.wegobackend.common.exception.AppException;

public class InvalidResetTokenException extends AppException {

    public InvalidResetTokenException() {
        super(AppErrorCode.INVALID_RESET_TOKEN);
    }
}