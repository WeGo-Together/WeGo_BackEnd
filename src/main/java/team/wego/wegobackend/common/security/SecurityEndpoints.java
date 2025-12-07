package team.wego.wegobackend.common.security;

public class SecurityEndpoints {

    public static final String[] PUBLIC_PATTERNS = {
        "/api/v*/auth/**",
        "/api/v*/docs/**",
        "/api/v*/health",
        "/error"
    };
}
