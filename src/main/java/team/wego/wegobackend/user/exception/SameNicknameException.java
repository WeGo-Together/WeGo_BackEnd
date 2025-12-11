package team.wego.wegobackend.user.exception;

import team.wego.wegobackend.common.exception.AppErrorCode;
import team.wego.wegobackend.common.exception.AppException;

public class SameNicknameException extends AppException {

    public SameNicknameException() {
        super(AppErrorCode.ALREADY_EXIST_NICKNAME);
    }
}
