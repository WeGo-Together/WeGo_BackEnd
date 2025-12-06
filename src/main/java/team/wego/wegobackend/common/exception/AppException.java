package team.wego.wegobackend.common.exception;

import lombok.AccessLevel;
import lombok.Getter;

@Getter(AccessLevel.PUBLIC)
public class AppException extends RuntimeException {

    private final ErrorCode errorCode;
    private final transient Object[] args;

    public AppException(ErrorCode errorCode, Object... args) {
        super(errorCode.format(args));
        this.errorCode = errorCode;
        this.args = args;
    }

    public AppException(ErrorCode errorCode) {
        super(errorCode.getMessageTemplate());
        this.errorCode = errorCode;
        this.args = null;
    }

    public AppException(ErrorCode errorCode, Throwable cause, Object... args) {
        super(errorCode.format(args), cause);
        this.errorCode = errorCode;
        this.args = args;
    }

    public AppException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessageTemplate(), cause);
        this.errorCode = errorCode;
        this.args = null;
    }
}
