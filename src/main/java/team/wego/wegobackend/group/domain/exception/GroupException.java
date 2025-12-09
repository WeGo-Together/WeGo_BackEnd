package team.wego.wegobackend.group.domain.exception;

import team.wego.wegobackend.common.exception.AppException;
import team.wego.wegobackend.common.exception.ErrorCode;

public class GroupException extends AppException {

    public GroupException(ErrorCode errorCode, Object... args) {
        super(errorCode, args);
    }

    public GroupException(ErrorCode errorCode) {
        super(errorCode);
    }
}

