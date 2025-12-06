package team.wego.wegobackend.common.exception;

import org.springframework.http.HttpStatus;

public interface ErrorCode {

    HttpStatus getHttpStatus();

    String getMessageTemplate();

    default String format(Object... args) {
        return String.format(getMessageTemplate(), args);
    }
}

