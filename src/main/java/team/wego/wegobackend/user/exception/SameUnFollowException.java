package team.wego.wegobackend.user.exception;

import team.wego.wegobackend.common.exception.AppErrorCode;
import team.wego.wegobackend.common.exception.AppException;

public class SameUnFollowException extends AppException {

    public SameUnFollowException() {
        super(AppErrorCode.NOT_SAME_UNFOLLOW);
    }
}
