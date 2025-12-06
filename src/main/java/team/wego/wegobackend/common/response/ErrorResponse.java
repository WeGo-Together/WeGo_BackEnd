package team.wego.wegobackend.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import org.springframework.http.HttpStatus;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        String type,              // RFC: 문제 유형 URL
        String title,             // RFC: 짧은 오류 요약 (심볼릭 코드명)
        int status,               // HTTP 상태 코드
        String detail,            // RFC: 상세 메시지
        String instance,          // RFC: 문제 발생 리소스(요청 경로)
        String errorCode,         // 서비스 내부 비즈니스 코드 (예: U001/C001 등)
        List<FieldError> data     // 필드 단위 검증 에러 목록 (확장 필드)
) {

    public static ErrorResponse of(
            HttpStatus status,
            String message,
            List<FieldError> data) {
        return new ErrorResponse(
                null,
                null,
                status.value(),
                message,
                null,
                null,
                (data == null || data.isEmpty()) ? null : data
        );
    }

    public static ErrorResponse of(
            HttpStatus status,
            String message) {
        return new ErrorResponse(
                null,
                null,
                status.value(),
                message,
                null,
                null,
                null
        );
    }


    public static ErrorResponse of(
            HttpStatus status,
            String message,
            String errorCode) {
        return new ErrorResponse(
                null,
                errorCode,
                status.value(),
                message,
                null,
                errorCode,
                null
        );
    }


    public static ErrorResponse of(
            HttpStatus status,
            String message,
            String errorCode,
            List<FieldError> data) {

        return new ErrorResponse(
                null,
                errorCode,
                status.value(),
                message,
                null,
                errorCode,
                (data == null || data.isEmpty()) ? null : data
        );
    }

    public static ErrorResponse of(String type,
            String title,
            HttpStatus status,
            String detail,
            String instance,
            String errorCode,
            List<FieldError> data) {

        return new ErrorResponse(
                type,
                title,
                status.value(),
                detail,
                instance,
                errorCode,
                (data == null || data.isEmpty()) ? null : data
        );
    }

    public record FieldError(
            String field,
            String reason) {

        public static FieldError of(String field, String reason) {
            return new FieldError(field, reason);
        }
    }
}
