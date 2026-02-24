package team.wego.wegobackend.common.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
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
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import team.wego.wegobackend.common.response.ErrorResponse;
import team.wego.wegobackend.common.response.ErrorResponse.FieldError;
import team.wego.wegobackend.common.security.exception.DuplicateSessionException;
import team.wego.wegobackend.common.security.exception.ExpiredTokenException;
import team.wego.wegobackend.common.security.exception.InvalidTokenException;
import team.wego.wegobackend.group.domain.exception.GroupErrorCode;

@Slf4j(topic = "GlobalExceptionHandler")
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String PROBLEM_BASE_URI = "about:blank";

    @ExceptionHandler(ExpiredTokenException.class)
    public ResponseEntity<ErrorResponse> handleExpiredToken(ExpiredTokenException ex,
            HttpServletRequest request) {
        return handleApp(new AppException(AppErrorCode.EXPIRED_TOKEN), request);
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ErrorResponse> handleInvalidToken(InvalidTokenException ex,
            HttpServletRequest request) {
        return handleApp(new AppException(AppErrorCode.INVALID_TOKEN), request);
    }

    @ExceptionHandler(DuplicateSessionException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateSession(DuplicateSessionException ex,
            HttpServletRequest request) {
        return handleApp(new AppException(AppErrorCode.DUPLICATE_LOGIN), request);
    }

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


    @ExceptionHandler({RedisConnectionFailureException.class, RedisSystemException.class})
    public ResponseEntity<ErrorResponse> handleRedis(Exception ex, HttpServletRequest request) {
        log.error("Redis 장애(500): {}", rootCauseMessage(ex), ex);
        return handleApp(new AppException(GroupErrorCode.REDIS_READ_FAILED), request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(
            DataIntegrityViolationException ex, HttpServletRequest request) {

        String msg = rootCauseMessage(ex);
        log.error("DB 무결성 위반(409): {}", msg, ex);

        // 예: H2 메시지에 constraint 이름이 들어옴
        // "PUBLIC.UK_GROUP_ID_SORT_ORDER_INDEX_D"
        if (msg != null && msg.contains("UK_GROUP_ID_SORT_ORDER_INDEX_D")) {
            return handleApp(new AppException(GroupErrorCode.GROUP_IMAGE_SORT_ORDER_CONFLICT),
                    request);
        }

        // 나머지는 공통 무결성 위반 코드로 (AppErrorCode 하나 만드는 걸 추천)
        return handleApp(new AppException(AppErrorCode.DATA_INTEGRITY_VIOLATION), request);
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ErrorResponse> handleDataAccess(
            DataAccessException ex, HttpServletRequest request) {

        log.error("DB 접근 오류(500): {}", rootCauseMessage(ex), ex);
        return handleApp(new AppException(AppErrorCode.INTERNAL_SERVER_ERROR), request);
    }

    @ExceptionHandler(JsonProcessingException.class)
    public ResponseEntity<ErrorResponse> handleJson(JsonProcessingException ex,
            HttpServletRequest request) {
        log.error("Jackson 직렬화/역직렬화 실패(500): {}", rootCauseMessage(ex), ex);
        AppException mapped = new AppException(GroupErrorCode.REDIS_READ_FAILED);
        return handleApp(mapped, request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request
    ) {
        String paramName = ex.getName();          // myStatuses
        Object rejectedValue = ex.getValue();     // CANCEL

        // List<Enum> 같은 제네릭까지 포함해서 enum 타입을 최대한 정확히 뽑아냄
        Class<? extends Enum<?>> enumType = resolveEnumType(ex);

        String allowed = null;
        if (enumType != null) {
            allowed = Arrays.stream(enumType.getEnumConstants())
                    .map(Enum::name)
                    .collect(Collectors.joining(", "));
        }

        AppErrorCode code = AppErrorCode.INVALID_INPUT_VALUE;
        String title = code.name();
        String type = toProblemType(title);
        String instance = request.getRequestURI();

        // 사람용 detail
        String detail = (allowed == null)
                ? String.format("요청 파라미터 '%s' 값이 올바르지 않습니다. 입력값=%s", paramName, rejectedValue)
                : String.format("요청 파라미터 '%s' 값이 올바르지 않습니다. 입력값=%s, 허용값=[%s]",
                        paramName, rejectedValue, allowed);

        // 프론트/QA용 errors (field 단위)
        List<ErrorResponse.FieldError> errors = List.of(
                ErrorResponse.FieldError.of(
                        paramName,
                        (allowed == null)
                                ? String.format("허용되지 않는 값입니다. 입력값=%s", rejectedValue)
                                : String.format("허용되지 않는 값입니다. 입력값=%s, 허용값=[%s]", rejectedValue,
                                        allowed)
                )
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(
                        type,
                        title,
                        HttpStatus.BAD_REQUEST,
                        detail,
                        instance,
                        title,
                        errors
                ));
    }

    /**
     * MethodArgumentTypeMismatchException에서 - requiredType이 enum이면 그대로 사용 - List<Enum> 같은 경우 제네릭
     * 타입을 파서 실제 enum 타입을 추출
     */
    @SuppressWarnings("unchecked")
    private static Class<? extends Enum<?>> resolveEnumType(
            MethodArgumentTypeMismatchException ex) {
        // 1) requiredType 자체가 enum인 경우
        Class<?> requiredType = ex.getRequiredType();
        if (requiredType != null && requiredType.isEnum()) {
            return (Class<? extends Enum<?>>) requiredType;
        }

        // 2) Controller 파라미터가 List<Enum>인 경우 (myStatuses, includeStatuses, excludeStatuses)
        if (ex.getParameter() != null) {
            Type generic = ex.getParameter().getGenericParameterType();
            if (generic instanceof ParameterizedType pt) {
                Type[] args = pt.getActualTypeArguments();
                if (args.length == 1 && args[0] instanceof Class<?> argClass && argClass.isEnum()) {
                    return (Class<? extends Enum<?>>) argClass;
                }
            }
        }

        return null;
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
