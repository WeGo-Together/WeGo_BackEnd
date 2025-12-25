package team.wego.wegobackend.group.v2.application.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;
import team.wego.wegobackend.group.v2.application.event.GroupCreatedEvent;
import team.wego.wegobackend.group.v2.application.handler.GroupCreateNotificationHandler;

@Slf4j
@Component
@RequiredArgsConstructor
public class GroupNotificationEventListener {

    private final GroupCreateNotificationHandler handler;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onGroupCreated(GroupCreatedEvent event) {
        log.info("[NOTI] onGroupCreated event received. groupId={}, hostId={}", event.groupId(), event.hostUserId());
        handler.handle(event.groupId(), event.hostUserId());
    }
}

