package team.wego.wegobackend.common.security.exception;

import team.wego.wegobackend.common.exception.AppErrorCode;

public class DuplicateSessionException extends RuntimeException {

    public DuplicateSessionException() {
        super(AppErrorCode.DUPLICATE_LOGIN.getMessageTemplate());
    }
}