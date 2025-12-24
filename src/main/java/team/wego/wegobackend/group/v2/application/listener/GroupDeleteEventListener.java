package team.wego.wegobackend.group.v2.application.listener;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import team.wego.wegobackend.group.v2.application.event.GroupDeletedEvent;
import team.wego.wegobackend.group.v2.application.handler.GroupDeleteNotificationHandler;

@Component
@RequiredArgsConstructor
public class GroupDeleteEventListener {

    private final GroupDeleteNotificationHandler handler;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onDeleted(GroupDeletedEvent event) {
        handler.handle(event.groupId(), event.hostId(), event.attendeeUserIds());
    }
}

