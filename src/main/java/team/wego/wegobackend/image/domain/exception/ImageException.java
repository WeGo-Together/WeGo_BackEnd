package team.wego.wegobackend.image.domain.exception;

import team.wego.wegobackend.common.exception.AppException;
import team.wego.wegobackend.common.exception.ErrorCode;

public class ImageException extends AppException {

    public ImageException(ErrorCode errorCode, Object... args) {
        super(errorCode, args);
    }

    public ImageException(ErrorCode errorCode) {
        super(errorCode);
    }

    public ImageException(ErrorCode errorCode, Throwable cause, Object... args) {
        super(errorCode, cause, args);
    }

    public ImageException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
