package team.wego.wegobackend.common.security;

public class SecurityEndpoints {

    public static final String[] PUBLIC_PATTERNS = {
        "/api/v*/auth/**",
        "/api/v*/health",
        "/h2-console/**",
        "/error",

        "/swagger-ui/**",
        "/swagger-ui.html",

        // API Docs
        "/api-docs/**",
        "/v*/api-docs/**",
    };

}
