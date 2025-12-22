package team.wego.wegobackend.group.domain.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import team.wego.wegobackend.common.exception.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum GroupErrorCode implements ErrorCode {
    NO_PERMISSION_TO_REJECT_JOIN(HttpStatus.FORBIDDEN,
            "모임: 참여 거절 권한이 없습니다. 모임 ID: %s 회원 ID: %s"
    ),
    CANNOT_APPROVE_SELF(HttpStatus.BAD_REQUEST,
            "모임: 자기 자신을 승인할 수 없습니다. 모임 ID: %s 회원 ID: %s"
    ),
    CANNOT_REJECT_SELF(HttpStatus.BAD_REQUEST,
            "모임: 자기 자신을 거절할 수 없습니다. 모임 ID: %s 회원 ID: %s"
    ),
    GROUP_USER_STATUS_NOT_ALLOWED_TO_APPROVE(HttpStatus.BAD_REQUEST,
            "모임: 승인 처리는 PENDING 상태에서만 가능합니다. 모임 ID: %s 회원 ID: %s 현재 상태: %s"
    ),

    GROUP_USER_STATUS_NOT_ALLOWED_TO_REJECT(HttpStatus.BAD_REQUEST,
            "모임: 거절 처리는 PENDING 상태에서만 가능합니다. 모임 ID: %s 회원 ID: %s 현재 상태: %s"
    ),
    GROUP_JOIN_POLICY_NOT_APPROVAL_REQUIRED(HttpStatus.BAD_REQUEST,
            "모임: 승인제 모임이 아니어서 승인/거절이 불가능합니다. 모임 ID: %s 참여 방식: %s"
    ),

    GROUP_CANNOT_APPROVE_IN_STATUS(HttpStatus.BAD_REQUEST,
            "모임: 현재 모임 상태에서는 승인 처리가 불가능합니다. 모임 ID: %s 모임 상태: %s"
    ),

    GROUP_CANNOT_REJECT_IN_STATUS(HttpStatus.BAD_REQUEST,
            "모임: 현재 모임 상태에서는 거절 처리가 불가능합니다. 모임 ID: %s 모임 상태: %s"
    ),

    NO_PERMISSION_TO_APPROVE_JOIN(HttpStatus.FORBIDDEN,
            "모임: 참여 승인/거절 권한이 없습니다. 모임 ID: %s 회원 ID: %s"
    ),

    GROUP_USER_NOT_PENDING_STATUS(HttpStatus.BAD_REQUEST,
            "모임: 승인/거절은 PENDING 상태에서만 가능합니다. 모임 ID: %s 회원 ID: %s 현재 상태: %s"
    ),

    // 필요하면(선택): 이미 처리된 요청을 더 명확히 구분하고 싶을 때
    GROUP_JOIN_REQUEST_ALREADY_PROCESSED(HttpStatus.BAD_REQUEST,
            "모임: 이미 처리된 참여 요청입니다. 모임 ID: %s 회원 ID: %s 현재 상태: %s"
    ),
    GROUP_CANNOT_LEAVE_IN_STATUS(HttpStatus.BAD_REQUEST,
            "모임: 현재 모임 상태에서는 나가기/신청취소가 불가능합니다. 모임 ID: %s 모임 상태: %s"
    ),

    GROUP_USER_STATUS_NOT_ALLOWED_TO_LEAVE(HttpStatus.BAD_REQUEST,
            "모임: 현재 멤버십 상태에서는 나가기/신청취소가 불가능합니다. 모임 ID: %s 회원 ID: %s 상태: %s"
    ),

    GROUP_NOT_PENDING_STATUS(HttpStatus.BAD_REQUEST,
            "모임: 승인 대기(PENDING) 상태가 아닙니다. 모임 ID: %s 회원 ID: %s 현재 상태: %s"
    ),

    ALREADY_LEFT_GROUP(HttpStatus.CONFLICT,
            "모임: 이미 나간 상태입니다. 모임 ID: %s 회원 ID: %s"
    ),

    ALREADY_CANCELLED_JOIN_REQUEST(HttpStatus.CONFLICT,
            "모임: 이미 신청 취소한 상태입니다. 모임 ID: %s 회원 ID: %s"
    ),

    GROUP_KICKED_USER(HttpStatus.FORBIDDEN,
            "모임: 추방된 회원은 나가기/신청취소를 할 수 없습니다. 모임 ID: %s 회원 ID: %s"
    ),
    GROUP_REJECTED_USER(HttpStatus.FORBIDDEN,
            "모임: 가입 신청이 거절된 회원은 나가기/신청취소를 할 수 없습니다. 모임 ID: %s 회원 ID: %s"
    ),
    INVALID_JOIN_POLICY(HttpStatus.BAD_REQUEST, "모임: 유효하지 않은 모임 가입 정책입니다. 모임 정책: %s"),
    JOIN_POLICY_NULL(HttpStatus.NOT_FOUND, "모임: 모임 가입 정책이 null 입니다."),
    GROUP_IMAGE_SORT_ORDER_CONFLICT(
            HttpStatus.CONFLICT,
            "모임: 이미지 정렬 처리 중 충돌이 발생했습니다. 다시 시도해주세요."
    ),
    GROUP_IMAGE_NOT_FOUND_IN_GROUP_AFTER_UPDATE(
            HttpStatus.BAD_REQUEST,
            "모임: 요청한 이미지 키(%s)를 반영할 수 없습니다. (모임에 없거나 선업로드 이미지가 아닙니다.)"
    ),
    TAG_EXCEED_MAX(HttpStatus.BAD_REQUEST, "모임: 태그는 최대 10개까지 가능합니다. (요청=%s)"),
    TAG_DUPLICATED(HttpStatus.BAD_REQUEST, "모임: 태그가 중복되었습니다."),
    DUPLICATED_IMAGE_KEY_IN_REQUEST(HttpStatus.BAD_REQUEST, "모임: 이미지 키가 중복되었습니다."),
    IMAGE_ORDER_EMPTY_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "모임: 이미지는 최소 1장 이상이어야 합니다."),
    TAG_NAME_DUPLICATED(HttpStatus.BAD_REQUEST, "모임: 태그 이름이 중복되었습니다."),

    GROUP_TITLE_REQUIRED(HttpStatus.BAD_REQUEST, "모임: 제목은 필수입니다."),
    GROUP_TITLE_TOO_LONG(HttpStatus.BAD_REQUEST, "모임: 제목은 50자 이하여야 합니다."),
    GROUP_DESCRIPTION_REQUIRED(HttpStatus.BAD_REQUEST, "모임: 설명은 필수입니다."),
    GROUP_DESCRIPTION_TOO_LONG(HttpStatus.BAD_REQUEST, "모임: 설명은 300자 이하여야 합니다."),

    GROUP_TIME_REQUIRED(HttpStatus.BAD_REQUEST, "모임: 시작 시간은 필수입니다."),
    GROUP_TIME_INVALID_RANGE(HttpStatus.BAD_REQUEST, "모임: 시작/종료 시간이 올바르지 않습니다. (start < end)"),
    MAX_PARTICIPANTS_BELOW_ATTEND_COUNT(HttpStatus.CONFLICT, "모임: 현재 참석자 수(%s)보다 정원을 줄일 수 없습니다."),
    GROUP_ONLY_HOST_CAN_UPDATE(HttpStatus.FORBIDDEN, "모임: 수정 권한이 없습니다."),
    GROUP_CANNOT_UPDATE_IN_STATUS(HttpStatus.CONFLICT, "모임: 현재 상태(%s)에서는 수정할 수 없습니다."),
    GROUP_DELETED(HttpStatus.NOT_FOUND, "모임: 삭제된 모임입니다."),
    INVALID_COOLDOWN_SECONDS(HttpStatus.BAD_REQUEST, "모임: 유효하지 않은 쿨다운 정책입니다."),
    GROUP_CREATE_COOLDOWN_ACTIVE(HttpStatus.TOO_MANY_REQUESTS,
            "모임: 모임 생성은 연속으로 할 수 없습니다. {%s}초 후 다시 시도해 주세요."),
    PRE_UPLOADED_IMAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "모임: 선 업로드 이미지가 만료되었거나 존재하지 않습니다."),
    PRE_UPLOADED_IMAGE_OWNER_MISMATCH(HttpStatus.FORBIDDEN, "모임: 선 업로드 이미지를 업로드한 사용자만 사용할 수 있습니다."),
    REDIS_SAVE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "모임: 선 업로드 이미지 저장에 실패했습니다."),
    REDIS_READ_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "모임: 선 업로드 이미지 조회에 실패했습니다."),
    GROUP_IMAGE_KEY_REQUIRED(HttpStatus.BAD_REQUEST, "모임: 이미지 키는 필수입니다."),
    GROUP_IMAGE_VARIANT_REQUIRED(HttpStatus.BAD_REQUEST, "모임: 모임 variant는 필수입니다."),
    GROUP_BANNED_USER(HttpStatus.BAD_REQUEST, "차단된 사용자는 재참여할 수 없습니다."),
    GROUP_IMAGE_MUST_440_240(HttpStatus.BAD_REQUEST, "모임: 모임 이미지 440x240은 필수입니다."),
    GROUP_IMAGE_MUST_100_100(HttpStatus.BAD_REQUEST, "모임: 모임 이미지 100x100은 필수입니다."),
    GROUP_NOT_NULL(HttpStatus.BAD_REQUEST, "모임: 모임은 null일 수 없습니다."),
    HOST_USER_NOT_FOUND(HttpStatus.NOT_FOUND, "모임: 호스트 사용자를 찾을 수 없습니다: %s"),
    INVALID_TIME_RANGE(HttpStatus.BAD_REQUEST, "모임: 종료 시간은 시작 시간보다 뒤여야 합니다."),
    INVALID_MAX_PARTICIPANTS(HttpStatus.BAD_REQUEST, "모임: 최대 인원은 최소 2명 이상이어야 합니다."),
    TAG_NOT_FOUND(HttpStatus.NOT_FOUND, "모임: 요청한 태그를 찾을 수 없습니다."),
    IMAGE_UPLOAD_FAILED(HttpStatus.BAD_REQUEST, "모임: 이미지 업로드 요청에 실패했습니다: %s"),
    GROUP_NOT_FOUND_BY_ID(HttpStatus.NOT_FOUND, "모임: 모임을 찾을 수 없습니다. 모임 ID: %s"),
    IMAGE_UPLOAD_EXCEED(HttpStatus.BAD_REQUEST, "모임: 모임 이미지는 최대 3개 입니다. 현재 이미지 수: %s"),

    ALREADY_REQUESTED_TO_JOIN(HttpStatus.BAD_REQUEST, "모임: 이미 가입 요청한 상태입니다. 모임 ID: %s 회원 ID: %s"),
    GROUP_USER_NOT_FOUND(HttpStatus.NOT_FOUND, "모임: 회원을 찾을 수 없습니다. 회원 ID: %s"),
    ALREADY_ATTEND_GROUP(HttpStatus.BAD_REQUEST, "모임: 이미 참여 중인 모임입니다. 모임 ID: %s 회원 ID: %s"),
    GROUP_IS_FULL(HttpStatus.BAD_REQUEST, "모임: 모임 최대 참가자 수를 초과했습니다. 모임 ID: %s"),
    NOT_ATTEND_GROUP(HttpStatus.BAD_REQUEST, "모임: 참여한 적 없거나 이미 나간 상태입니다. 모임 ID: %s 회원 ID: %s"),
    GROUP_HOST_CANNOT_LEAVE(HttpStatus.BAD_REQUEST, "모임: HOST는 나갈 수 없습니다. 모임 ID: %s 회원 ID: %s"),
    NO_PERMISSION_TO_UPDATE_GROUP(HttpStatus.FORBIDDEN,
            "모임: 해당 모임을 수정할 권한이 없습니다. 모임 ID: %s 회원 ID: %s"),
    INVALID_MAX_PARTICIPANTS_LESS_THAN_CURRENT(HttpStatus.BAD_REQUEST,
            "모임: 현재 참여 인원 수(%s)보다 작은 값으로 최대 인원을 줄일 수 없습니다."),
    NO_PERMISSION_TO_DELETE_GROUP(HttpStatus.UNAUTHORIZED, "모임: 삭제할 수 있는 권한이 없습니다."),
    MY_GROUP_TYPE_NOT_NULL(HttpStatus.BAD_REQUEST, "모임: MyGroupType 값은 null일 수 없습니다."),
    INVALID_MY_GROUP_TYPE(HttpStatus.BAD_REQUEST, "지원하지 않는 MyGroupType: %s"),
    IMAGE_URL_REQUIRED(HttpStatus.BAD_REQUEST, "모임 이미지 삭제: url은 필수입니다."),
    GROUP_IMAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "모임 이미지가 존재하지 않습니다. groupId=%d"),
    LOCATION_REQUIRED(HttpStatus.BAD_REQUEST, "모임: 모임 위치는 필수입니다."),
    GROUP_STATUS_REQUIRED(HttpStatus.BAD_REQUEST, "모임: 모임 상태는 필수입니다."),
    GROUP_STATUS_TRANSFER_IMPOSSIBLE(HttpStatus.BAD_REQUEST,
            "모임: 상태 전이가 불가능합니다. 현재 상태: %s, 요청한 상태: %s"),
    USER_ID_NULL(HttpStatus.NOT_FOUND, "모임: 회원 ID가 null 입니다."),
    GROUP_HOST_CANNOT_ATTEND(HttpStatus.BAD_REQUEST, "모임: HOST는 다시 모임에 신청을 할 수 없습니다."),
    GROUP_NOT_RECRUITING(HttpStatus.BAD_REQUEST, "모임: 모집 상태가 아닙니다. 현재 상태: %s"),
    GROUP_NOT_ATTEND_STATUS(HttpStatus.BAD_REQUEST, "모임: ATTEND 상태에서만 LEFT 상태로 전이할 수 있습니다.");


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
