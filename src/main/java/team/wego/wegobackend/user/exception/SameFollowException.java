package team.wego.wegobackend.user.exception;

import team.wego.wegobackend.common.exception.AppErrorCode;
import team.wego.wegobackend.common.exception.AppException;
import team.wego.wegobackend.common.exception.ErrorCode;

public class SameFollowException extends AppException {

    public SameFollowException() {
        super(AppErrorCode.NOT_SAME_FOLLOW);
    }
}
