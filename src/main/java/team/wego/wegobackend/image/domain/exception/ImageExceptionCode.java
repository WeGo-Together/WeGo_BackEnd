package team.wego.wegobackend.image.domain.exception;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import team.wego.wegobackend.common.exception.ErrorCode;

@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
@Getter(AccessLevel.PUBLIC)
public enum ImageExceptionCode implements ErrorCode {

    INVALID_IMAGE_SIZE(HttpStatus.BAD_REQUEST,
            "이미지: 크기가 너무 큽니다. 최대 %,d bytes 까지만 허용됩니다."),

    UNSUPPORTED_IMAGE_CONTENT_TYPE(HttpStatus.BAD_REQUEST,
            "이미지: 허용되지 않은 이미지 타입입니다. contentType=%s"),

    MISSING_EXTENSION(HttpStatus.BAD_REQUEST,
            "이미지: 확장자가 없는 파일입니다."),

    UNSUPPORTED_EXTENSION(HttpStatus.BAD_REQUEST,
            "이미지: 허용되지 않은 확장자입니다. extension=%s"),

    INVALID_IMAGE_FORMAT(HttpStatus.BAD_REQUEST,
            "이미지: 이미지 파일 형식이 올바르지 않습니다."),

    RESIZE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR,
            "이미지: %s에 실패했습니다. format=%s"),

    WEBP_CONVERT_FAILED(HttpStatus.INTERNAL_SERVER_ERROR,
            "이미지: WebP 변환에 실패했습니다."),

    IMAGE_IO_ERROR(HttpStatus.INTERNAL_SERVER_ERROR,
            "이미지: %s 중 입출력 오류가 발생했습니다."),

    DIR_REQUIRED(HttpStatus.BAD_REQUEST,
            "이미지: dir은 필수입니다."),

    DIR_INVALID_TRAVERSAL(HttpStatus.BAD_REQUEST,
            "이미지: 잘못된 디렉토리 경로입니다."),

    DIR_TRAILING_SLASH(HttpStatus.BAD_REQUEST,
            "이미지: dir은 /로 끝나면 안 됩니다."),

    DIR_INVALID_PATTERN(HttpStatus.BAD_REQUEST,
            "이미지: dir에는 알파벳, 숫자, '-', '_', '/'만 사용할 수 있습니다.");

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
