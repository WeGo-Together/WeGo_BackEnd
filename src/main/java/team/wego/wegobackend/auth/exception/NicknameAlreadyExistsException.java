package team.wego.wegobackend.auth.exception;

import team.wego.wegobackend.common.exception.AppErrorCode;
import team.wego.wegobackend.common.exception.AppException;

public class NicknameAlreadyExistsException extends AppException {

    public NicknameAlreadyExistsException() {
        super(AppErrorCode.ALREADY_EXIST_NICKNAME);
    }
}
