package team.wego.wegobackend.chat.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final ChatProperties chatProperties;
    private final StompChannelInterceptor stompChannelInterceptor;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 클라이언트가 구독할 수 있는 목적지 prefix
        // /sub/chat/room/{roomId} - 채팅방 메시지 구독
        // /sub/user/{userId} - 개인 알림 구독
        registry.enableSimpleBroker("/sub");

        // 클라이언트가 메시지를 보낼 때 사용하는 prefix
        // /pub/chat/message - 메시지 전송
        registry.setApplicationDestinationPrefixes("/pub");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // WebSocket 연결 엔드포인트
        registry.addEndpoint(chatProperties.getWebsocket().getEndpoint())
                .setAllowedOriginPatterns(chatProperties.getWebsocket().getAllowedOrigins())
                .withSockJS();

        // SockJS 없이 연결 (네이티브 앱용)
        registry.addEndpoint(chatProperties.getWebsocket().getEndpoint())
                .setAllowedOriginPatterns(chatProperties.getWebsocket().getAllowedOrigins());
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(stompChannelInterceptor);
    }
}
