package team.wego.wegobackend.chat.domain.exception;

import team.wego.wegobackend.common.exception.AppException;
import team.wego.wegobackend.common.exception.ErrorCode;

public class ChatException extends AppException {

    public ChatException(ErrorCode errorCode, Object... args) {
        super(errorCode, args);
    }

    public ChatException(ErrorCode errorCode) {
        super(errorCode);
    }
}
