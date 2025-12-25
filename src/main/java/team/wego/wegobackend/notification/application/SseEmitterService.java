package team.wego.wegobackend.notification.application;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import team.wego.wegobackend.group.v2.application.event.NotificationEvent;
import team.wego.wegobackend.notification.application.dto.response.NotificationResponse;

@Slf4j
@Service
public class SseEmitterService {

    // 사용자별 Emitter 관리 (단일 서버라 메모리로 관리)
    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();
    private static final Long TIMEOUT = 60 * 1000 * 60L; // 60분

    public SseEmitter createEmitter(Long userId) {
        SseEmitter emitter = new SseEmitter(TIMEOUT);
        emitters.put(userId, emitter);

        // 연결 종료 시 제거
        emitter.onCompletion(() -> emitters.remove(userId));
        emitter.onTimeout(() -> emitters.remove(userId));
        emitter.onError((e) -> emitters.remove(userId));

        try {
            emitter.send(SseEmitter.event()
                    .name("connect")
                    .data("Connected"));
        } catch (IOException e) {
            emitter.completeWithError(e);
        }

        return emitter;
    }

    // 특정 사용자에게 알림 전송
    public void sendNotification(Long userId, NotificationResponse notification) {
        SseEmitter emitter = emitters.get(userId);
        log.debug("Connected emitter Info -> {} ", emitter);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name("notification")
                        .data(notification));
            } catch (IOException e) {
                emitters.remove(userId);
            }
        }
    }

    public void sendNotification(Long userId, NotificationEvent notification) {
        sendNotificationIfConnected(userId, notification);
    }

    public boolean sendNotificationIfConnected(Long userId, NotificationEvent notification) {
        SseEmitter emitter = emitters.get(userId);

        if (emitter == null) {
            log.debug("[SSE] no emitter. userId={}", userId);
            return false;
        }

        try {
            emitter.send(SseEmitter.event().name("notification").data(notification));
            log.debug("[SSE] sent. userId={} notificationId={}", userId, notification.getId());
            return true;
        } catch (IOException e) {
            log.warn("[SSE] send failed. userId={} reason={}", userId, e.toString());
            emitters.remove(userId);
            return false;
        }
    }
}
