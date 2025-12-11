package team.wego.wegobackend.user.exception;

import team.wego.wegobackend.common.exception.AppErrorCode;
import team.wego.wegobackend.common.exception.AppException;

public class ExistFollowException extends AppException {

    public ExistFollowException() {
        super(AppErrorCode.ALREADY_EXIST_FOLLOW);
    }
}
