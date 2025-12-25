package team.wego.wegobackend.group.v2.application.listener;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import team.wego.wegobackend.group.v2.application.event.GroupJoinedEvent;
import team.wego.wegobackend.group.v2.application.handler.GroupJoinNotificationHandler;

@Component
@RequiredArgsConstructor
public class GroupJoinEventListener {

    private final GroupJoinNotificationHandler handler;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onJoined(GroupJoinedEvent event) {
        handler.handle(event.groupId(), event.hostId(), event.joinerUserId());
    }
}

