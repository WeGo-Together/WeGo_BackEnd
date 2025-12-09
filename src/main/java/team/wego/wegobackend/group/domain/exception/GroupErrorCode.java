package team.wego.wegobackend.group.domain.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import team.wego.wegobackend.common.exception.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum GroupErrorCode implements ErrorCode {

    HOST_USER_NOT_FOUND(HttpStatus.NOT_FOUND, "모임: 호스트 사용자를 찾을 수 없습니다: %s"),
    INVALID_TIME_RANGE(HttpStatus.BAD_REQUEST, "모임: 종료 시간은 시작 시간보다 뒤여야 합니다."),
    INVALID_MAX_PARTICIPANTS(HttpStatus.BAD_REQUEST, "모임: 최대 인원은 최소 2명 이상이어야 합니다."),
    TAG_NOT_FOUND(HttpStatus.NOT_FOUND, "모임: 요청한 태그를 찾을 수 없습니다."),
    IMAGE_UPLOAD_FAILED(HttpStatus.BAD_REQUEST, "모임: 이미지 업로드 요청에 실패했습니다: %s"),
    GROUP_NOT_FOUND_BY_ID(HttpStatus.NOT_FOUND, "모임: 모임을 찾을 수 없습니다. 모임 ID: %s"),
    IMAGE_UPLOAD_EXCEED(HttpStatus.BAD_REQUEST, "모임: 모임 이미지는 최대 3개 입니다. 현재 이미지 수: %s"),

    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "모임: 회원을 찾을 수 없습니다. 회원 ID: %s"),
    ALREADY_ATTEND_GROUP(HttpStatus.BAD_REQUEST, "모임: 이미 참여 중인 모임입니다. 모임 ID: %s 회원 ID: %s"),
    GROUP_CAPACITY_EXCEEDED(HttpStatus.BAD_REQUEST, "모임: 모임 최대 참가자 수를 초과했습니다. 모임 ID: %s"),
    NOT_ATTEND_GROUP(HttpStatus.BAD_REQUEST, "모임: 참여한 적 없거나 이미 나간 상태입니다. 모임 ID: %s 회원 ID: %s"),
    HOST_CANNOT_LEAVE_OWN_GROUP(HttpStatus.BAD_REQUEST, "모임: HOST는 나갈 수 없습니다. 모임 ID: %s 회원 ID: %s");

    private final HttpStatus status;
    private final String message;

    @Override
    public HttpStatus getHttpStatus() {
        return status;
    }

    @Override
    public String getMessageTemplate() {
        return message;
    }
}
