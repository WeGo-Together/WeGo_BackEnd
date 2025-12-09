package team.wego.wegobackend.common.exception;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
@Getter(AccessLevel.PUBLIC)
public enum AppErrorCode implements ErrorCode {
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "공통: 서버 내부 오류가 발생했습니다."),
    SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "공통: 서비스가 일시적으로 불가능합니다."),
    DEPENDENCY_FAILURE(HttpStatus.BAD_GATEWAY, "공통: 외부/하위 시스템 연동에 실패했습니다."),
    IO_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "공통: 입출력 처리 중 오류가 발생했습니다."),
    REQUEST_CONTEXT_NOT_FOUND(HttpStatus.NOT_FOUND, "공통: 요청 컨텍스트를 찾을 수 없습니다."),

    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "공통: 잘못된 입력입니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "공통: 허용되지 않은 HTTP 메서드입니다."),
    UNSUPPORTED_MEDIA_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "공통: 지원하지 않는 콘텐츠 타입입니다."),

    ENTITY_NOT_FOUND(HttpStatus.NOT_FOUND, "공통: 요청한 리소스를 찾을 수 없습니다."),
    RESP_BODY_WRITE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "공통: 응답 본문을 생성/쓰기 중 오류가 발생했습니다."),
    MEDIA_TYPE_NOT_ACCEPTABLE(HttpStatus.NOT_ACCEPTABLE, "공통: 요청한 응답 형식을 제공할 수 없습니다."),

    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "회원 : 가입된 회원이 아닙니다."),
    ALREADY_EXISTS_USER(HttpStatus.BAD_REQUEST, "회원 : 이미 가입한 회원입니다."),
    INVALID_PASSWORD_VALUE(HttpStatus.BAD_REQUEST, "회원 : 비밀번호가 일치하지 않습니다."),
    DELETED_USER(HttpStatus.BAD_REQUEST, "회원 : 탈퇴한 회원입니다."),

    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "인증 : 만료된 토큰입니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "인증 : 유효하지 않은 토큰입니다.")
    ;

    private final HttpStatus httpStatus;
    private final String messageTemplate;

    @Override
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    @Override
    public String getMessageTemplate() {
        return messageTemplate;
    }
}

