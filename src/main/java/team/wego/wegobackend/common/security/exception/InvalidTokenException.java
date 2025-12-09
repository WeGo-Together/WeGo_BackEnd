package team.wego.wegobackend.common.security.exception;

import team.wego.wegobackend.common.exception.AppErrorCode;

public class InvalidTokenException extends RuntimeException {

    public InvalidTokenException() {
        super(AppErrorCode.INVALID_TOKEN.getMessageTemplate());
    }
}
