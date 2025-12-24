package team.wego.wegobackend.group.v2.application.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import team.wego.wegobackend.group.v2.application.event.GroupLeftEvent;
import team.wego.wegobackend.group.v2.application.handler.GroupLeaveNotificationHandler;

@Slf4j
@Component
@RequiredArgsConstructor
public class GroupLeaveEventListener {

    private final GroupLeaveNotificationHandler handler;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onLeft(GroupLeftEvent event) {
        log.info("[EVENT][RECEIVE] {}", event.getClass().getName());
        handler.handle(event.groupId(), event.hostId(), event.leaverUserId());
    }
}

