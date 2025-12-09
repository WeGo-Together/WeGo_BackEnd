package team.wego.wegobackend.common.security.exception;

import team.wego.wegobackend.common.exception.AppErrorCode;

public class ExpiredTokenException extends RuntimeException {

    public ExpiredTokenException() {
        super(AppErrorCode.EXPIRED_TOKEN.getMessageTemplate());
    }
}
