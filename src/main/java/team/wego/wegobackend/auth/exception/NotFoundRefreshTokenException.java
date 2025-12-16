package team.wego.wegobackend.auth.exception;

import team.wego.wegobackend.common.exception.AppErrorCode;
import team.wego.wegobackend.common.exception.AppException;

public class NotFoundRefreshTokenException extends AppException {

    public NotFoundRefreshTokenException() {
        super(AppErrorCode.NOT_FOUND_TOKEN);
    }

}
