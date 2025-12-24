package team.wego.wegobackend.group.v2.application.listener;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import team.wego.wegobackend.group.v2.application.event.GroupJoinApprovedEvent;
import team.wego.wegobackend.group.v2.application.event.GroupJoinRejectedEvent;
import team.wego.wegobackend.group.v2.application.handler.GroupJoinDecisionNotificationHandler;

@Component
@RequiredArgsConstructor
public class GroupJoinDecisionEventListener {

    private final GroupJoinDecisionNotificationHandler handler;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onApproved(GroupJoinApprovedEvent event) {
        handler.handleApproved(event.groupId(), event.approverUserId(), event.targetUserId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onRejected(GroupJoinRejectedEvent event) {
        handler.handleRejected(event.groupId(), event.approverUserId(), event.targetUserId());
    }
}
