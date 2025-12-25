package team.wego.wegobackend.group.v2.application.event;

import java.util.List;

public record GroupDeletedEvent(
        // deleteHard()에서 삭제 전에 group/host 정보를 꺼내서 이벤트에 실어 보내면,
        //AFTER_COMMIT에서도 DB 재조회가 필요 없어진다.
        Long groupId,
        Long hostId,
        String hostNickName,
        String groupTitle,
        List<Long> attendeeUserIds
) {

}

