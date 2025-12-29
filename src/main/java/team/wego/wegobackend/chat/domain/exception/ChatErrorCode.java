package team.wego.wegobackend.chat.domain.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import team.wego.wegobackend.common.exception.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum ChatErrorCode implements ErrorCode {

    // 채팅방 관련
    CHAT_ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "채팅방을 찾을 수 없습니다"),
    CHAT_ROOM_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 존재하는 채팅방입니다"),
    CHAT_ROOM_EXPIRED(HttpStatus.GONE, "만료된 채팅방입니다"),

    // 참여자 관련
    NOT_CHAT_PARTICIPANT(HttpStatus.FORBIDDEN, "채팅방에 참여하지 않았습니다"),
    ALREADY_CHAT_PARTICIPANT(HttpStatus.CONFLICT, "이미 참여 중인 채팅방입니다"),
    NOT_CHAT_ROOM_OWNER(HttpStatus.FORBIDDEN, "방장만 사용할 수 있는 기능입니다"),
    CANNOT_KICK_SELF(HttpStatus.BAD_REQUEST, "자기 자신을 추방할 수 없습니다"),
    PARTICIPANT_KICKED(HttpStatus.FORBIDDEN, "채팅방에서 추방되었습니다"),

    // 메시지 관련
    MESSAGE_TOO_LONG(HttpStatus.BAD_REQUEST, "메시지가 너무 깁니다 (최대 %d자)"),
    MESSAGE_EMPTY(HttpStatus.BAD_REQUEST, "메시지 내용을 입력해주세요"),

    // DM 관련
    CANNOT_DM_SELF(HttpStatus.BAD_REQUEST, "자기 자신에게 메시지를 보낼 수 없습니다"),
    DM_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 존재하는 1:1 채팅방입니다");

    private final HttpStatus httpStatus;
    private final String messageTemplate;
}
