package team.wego.wegobackend.auth.exception;

import team.wego.wegobackend.common.exception.AppErrorCode;
import team.wego.wegobackend.common.exception.AppException;

public class DeletedUserException extends AppException {

    public DeletedUserException() {
        super(AppErrorCode.DELETED_USER);
    }
}
