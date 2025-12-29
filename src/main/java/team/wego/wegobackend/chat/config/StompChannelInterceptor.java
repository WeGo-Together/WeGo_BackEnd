package team.wego.wegobackend.chat.config;

import java.security.Principal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;
import team.wego.wegobackend.common.security.jwt.JwtTokenProvider;

@Component
@RequiredArgsConstructor
@Slf4j
public class StompChannelInterceptor implements ChannelInterceptor {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader(AUTHORIZATION_HEADER);

            if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
                String token = authHeader.substring(BEARER_PREFIX.length());

                try {
                    if (jwtTokenProvider.validateAccessToken(token)) {
                        Long userId = jwtTokenProvider.getTokenUserId(token);
                        String email = jwtTokenProvider.getEmailFromToken(token);

                        // Principal로 사용자 정보 설정
                        StompPrincipal principal = new StompPrincipal(userId, email);
                        accessor.setUser(principal);

                        log.debug("WebSocket 연결 인증 성공 - userId: {}", userId);
                    }
                } catch (Exception e) {
                    log.error("WebSocket 연결 인증 실패", e);
                    throw new IllegalArgumentException("Invalid token");
                }
            } else {
                log.warn("WebSocket 연결 시 Authorization 헤더 없음");
                throw new IllegalArgumentException("Missing authorization header");
            }
        }

        return message;
    }

    /**
     * WebSocket 연결에서 사용하는 Principal 구현
     */
    public record StompPrincipal(Long userId, String email) implements Principal {
        @Override
        public String getName() {
            return String.valueOf(userId);
        }
    }
}
