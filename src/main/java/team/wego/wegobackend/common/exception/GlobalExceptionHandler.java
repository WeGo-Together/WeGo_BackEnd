package team.wego.wegobackend.common.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import team.wego.wegobackend.common.response.ErrorResponse;
import team.wego.wegobackend.common.response.ErrorResponse.FieldError;
import team.wego.wegobackend.group.domain.exception.GroupErrorCode;

@Slf4j(topic = "GlobalExceptionHandler")
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String PROBLEM_BASE_URI = "about:blank";

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ErrorResponse> handleApp(AppException ex,
            HttpServletRequest request) {
        ErrorCode code = ex.getErrorCode();

        String title = ((Enum<?>) code).name();
        String type = toProblemType(title);
        String instance = request.getRequestURI();
        String errorCode = title;

        return ResponseEntity.status(code.getHttpStatus())
                .body(ErrorResponse.of(
                        type,
                        title,
                        code.getHttpStatus(),
                        ex.getMessage(),
                        instance,
                        errorCode,
                        null
                ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleInvalid(MethodArgumentNotValidException ex,
            HttpServletRequest request) {
        List<FieldError> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> FieldError.of(err.getField(), err.getDefaultMessage()))
                .toList();

        AppErrorCode code = AppErrorCode.INVALID_INPUT_VALUE;
        String title = code.name();  // INVALID_INPUT_VALUE
        String type = toProblemType(title);
        String instance = request.getRequestURI();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(
                        type,
                        title,
                        HttpStatus.BAD_REQUEST,
                        code.getMessageTemplate(),
                        instance,
                        title,
                        errors
                ));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraint(ConstraintViolationException ex,
            HttpServletRequest request) {
        List<ErrorResponse.FieldError> errors = ex.getConstraintViolations().stream()
                .map(v -> ErrorResponse.FieldError.of(
                        v.getPropertyPath().toString(),
                        v.getMessage()))
                .toList();

        AppErrorCode code = AppErrorCode.INVALID_INPUT_VALUE;
        String title = code.name();
        String type = toProblemType(title);
        String instance = request.getRequestURI();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(
                        type,
                        title,
                        HttpStatus.BAD_REQUEST,
                        code.getMessageTemplate(),
                        instance,
                        title,
                        errors
                ));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleNotReadable(HttpMessageNotReadableException ex,
            HttpServletRequest request) {
        log.error("잘못된 JSON 본문(파싱 실패/형식 오류)(400): {}", rootCauseMessage(ex), ex);

        AppErrorCode code = AppErrorCode.INVALID_INPUT_VALUE;
        String title = code.name();
        String type = toProblemType(title);
        String instance = request.getRequestURI();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(
                        type,
                        title,
                        HttpStatus.BAD_REQUEST,
                        code.getMessageTemplate(),
                        instance,
                        title,
                        null
                ));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotAllowed(
            HttpRequestMethodNotSupportedException ex,
            HttpServletRequest request) {
        log.error("지원하지 않는 HTTP 메서드(405): {}", rootCauseMessage(ex), ex);

        AppErrorCode code = AppErrorCode.METHOD_NOT_ALLOWED;
        String title = code.name();
        String type = toProblemType(title);
        String instance = request.getRequestURI();

        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(ErrorResponse.of(
                        type,
                        title,
                        HttpStatus.METHOD_NOT_ALLOWED,
                        code.getMessageTemplate(),
                        instance,
                        title,
                        null
                ));
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(EntityNotFoundException ex,
            HttpServletRequest request) {
        log.error("JPA 엔티티 미발견(404): {}", rootCauseMessage(ex), ex);
        AppErrorCode code = AppErrorCode.ENTITY_NOT_FOUND;
        String title = code.name();
        String type = toProblemType(title);
        String instance = request.getRequestURI();

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of(
                        type,
                        title,
                        HttpStatus.NOT_FOUND,
                        code.getMessageTemplate(),
                        instance,
                        title,
                        null
                ));
    }

    @ExceptionHandler(HttpMessageNotWritableException.class)
    public ResponseEntity<ErrorResponse> handleNotWritable(
            HttpMessageNotWritableException ex,
            HttpServletRequest request) {
        log.error("응답 직렬화 실패(500): {}", rootCauseMessage(ex), ex);

        AppErrorCode code = AppErrorCode.RESP_BODY_WRITE_ERROR;
        String title = code.name();
        String type = toProblemType(title);
        String instance = request.getRequestURI();

        return ResponseEntity.status(code.getHttpStatus())
                .body(ErrorResponse.of(
                        type,
                        title,
                        code.getHttpStatus(),
                        code.getMessageTemplate(),
                        instance,
                        title,
                        null
                ));
    }

    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    public ResponseEntity<ErrorResponse> handleNotAcceptable(
            HttpMediaTypeNotAcceptableException ex,
            HttpServletRequest request) {
        log.warn("콘텐츠 협상 실패(406): {}", rootCauseMessage(ex));

        AppErrorCode code = AppErrorCode.MEDIA_TYPE_NOT_ACCEPTABLE;
        String title = code.name();
        String type = toProblemType(title);
        String instance = request.getRequestURI();

        return ResponseEntity.status(code.getHttpStatus())
                .body(ErrorResponse.of(
                        type,
                        title,
                        code.getHttpStatus(),
                        code.getMessageTemplate(),
                        instance,
                        title,
                        null
                ));
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleUnsupported(
            HttpMediaTypeNotSupportedException ex,
            HttpServletRequest request) {
        log.warn("미지원 콘텐츠 타입(415): {}", rootCauseMessage(ex));

        AppErrorCode code = AppErrorCode.UNSUPPORTED_MEDIA_TYPE;
        String title = code.name();
        String type = toProblemType(title);
        String instance = request.getRequestURI();

        return ResponseEntity.status(code.getHttpStatus())
                .body(ErrorResponse.of(
                        type,
                        title,
                        code.getHttpStatus(),
                        code.getMessageTemplate(),
                        instance,
                        title,
                        null
                ));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleNotParameter(
            MissingServletRequestParameterException ex,
            HttpServletRequest request) {
        log.warn("입력 파라메터 X (400): {}", rootCauseMessage(ex));

        AppErrorCode code = AppErrorCode.NOT_FOUND_PARAMETER;
        String title = code.name();
        String type = toProblemType(title);
        String instance = request.getRequestURI();

        return ResponseEntity.status(code.getHttpStatus())
                .body(ErrorResponse.of(
                        type,
                        title,
                        code.getHttpStatus(),
                        code.getMessageTemplate(),
                        instance,
                        title,
                        null
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAny(Exception ex, HttpServletRequest request) {
        log.error("처리되지 않은 예외(500): {}", rootCauseMessage(ex), ex);

        AppErrorCode code = AppErrorCode.INTERNAL_SERVER_ERROR;
        String title = code.name();
        String type = toProblemType(title);
        String instance = request.getRequestURI();

        return ResponseEntity.status(code.getHttpStatus())
                .body(ErrorResponse.of(
                        type,
                        title,
                        code.getHttpStatus(),
                        code.getMessageTemplate(),
                        instance,
                        title,
                        null
                ));
    }


    @ExceptionHandler({
            RedisConnectionFailureException.class,
            RedisSystemException.class,
            DataAccessException.class
    })
    public ResponseEntity<ErrorResponse> handleRedis(Exception ex, HttpServletRequest request) {
        log.error("Redis 장애(500): {}", rootCauseMessage(ex), ex);

        AppException mapped = new AppException(GroupErrorCode.REDIS_READ_FAILED);
        return handleApp(mapped, request);
    }

    @ExceptionHandler(JsonProcessingException.class)
    public ResponseEntity<ErrorResponse> handleJson(JsonProcessingException ex, HttpServletRequest request) {
        log.error("Jackson 직렬화/역직렬화 실패(500): {}", rootCauseMessage(ex), ex);
        AppException mapped = new AppException(GroupErrorCode.REDIS_READ_FAILED);
        return handleApp(mapped, request);
    }

    private static String rootCauseMessage(Throwable ex) {
        Throwable throwable = ex;
        while (throwable.getCause() != null) {
            throwable = throwable.getCause();
        }
        return throwable.getMessage();
    }

    private static String toProblemType(String title) {
        return PROBLEM_BASE_URI;
    }
}
