package team.wego.wegobackend.user.application.listener;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import team.wego.wegobackend.notification.application.dispatcher.NotificationDispatcher;
import team.wego.wegobackend.user.application.event.FollowEvent;

@Component
@RequiredArgsConstructor
public class FollowEventListener {

    private final NotificationDispatcher dispatcher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleFollowEvent(FollowEvent event) {

        //notification 저장
        dispatcher.dispatch(event.follower(), event.follow());
    }
}
