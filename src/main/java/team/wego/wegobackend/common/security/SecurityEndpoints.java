package team.wego.wegobackend.common.security;

public class SecurityEndpoints {

    public static final String[] PUBLIC_PATTERNS = {
        "/api/v*/auth/signup",
        "/api/v*/auth/login",
        "/api/v*/auth/google",
        "/api/v*/auth/refresh",
        "/api/v*/auth/password-reset/request",
        "/api/v*/auth/reset-verify",
        "/api/v*/auth/password-reset",
        "/api/v*/health",
        "/h2-console/**",
        "/error",

        //SpringDoc
        "/swagger-ui/**",
        "/swagger-ui.html",
        "/api-docs/**",
        "/v*/api-docs/**",

        // WebSocket (인증은 STOMP CONNECT에서 처리)
        "/ws-chat/**",

        // actuator
         "/actuator/**",
    };

}
