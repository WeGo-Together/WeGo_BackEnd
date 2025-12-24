package team.wego.wegobackend.group.v2.application.listener;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import team.wego.wegobackend.group.v2.application.event.GroupJoinRequestedEvent;
import team.wego.wegobackend.group.v2.application.handler.GroupJoinRequestNotificationHandler;

@Component
@RequiredArgsConstructor
public class GroupJoinRequestEventListener {

    private final GroupJoinRequestNotificationHandler handler;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onJoinRequested(GroupJoinRequestedEvent event) {
        handler.handle(event.groupId(), event.hostUserId(), event.requesterUserId());
    }
}

