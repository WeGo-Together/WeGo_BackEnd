package team.wego.wegobackend.group.v2.application.listener;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import team.wego.wegobackend.group.v2.application.event.GroupJoinKickedEvent;
import team.wego.wegobackend.group.v2.application.handler.GroupKickNotificationHandler;

@Component
@RequiredArgsConstructor
public class GroupKickEventListener {

    private final GroupKickNotificationHandler handler;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onKicked(GroupJoinKickedEvent event) {
        handler.handle(event.groupId(), event.hostId(), event.targetUserId());
    }
}
