package team.wego.wegobackend.auth.exception;

import team.wego.wegobackend.common.exception.AppErrorCode;
import team.wego.wegobackend.common.exception.AppException;

public class NotInitializedUserCounterException extends AppException {

    public NotInitializedUserCounterException() {
        super(AppErrorCode.NOT_INIT_COUNTER);
    }
}
