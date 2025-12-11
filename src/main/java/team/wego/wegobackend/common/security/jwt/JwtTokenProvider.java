package team.wego.wegobackend.common.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import team.wego.wegobackend.common.response.ErrorResponse;
import team.wego.wegobackend.common.security.exception.ExpiredTokenException;
import team.wego.wegobackend.common.security.exception.InvalidTokenException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secretKeyString;

    @Value("${jwt.access.token.expiration}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh.token.expiration}")
    private long refreshTokenExpiration;

    private SecretKey secretKey;

    private final ObjectMapper objectMapper;

    @PostConstruct
    protected void init() {
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKeyString));
    }

    /**
     * JWT 토큰 생성
     */
    public String createAccessToken(Long userId, String email, String role) {
        return createToken(userId, email, role, accessTokenExpiration, "access");
    }

    public String createRefreshToken(Long userId, String email) {
        return createToken(userId, email, null, refreshTokenExpiration, "refresh");
    }

    private String createToken(Long userId, String email, String role, long expiration, String type) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        JwtBuilder builder = Jwts.builder()
            .subject(email)
            .claim("userId", userId)
            .claim("type", type)
            .issuedAt(now)
            .expiration(expiryDate);

        if (role != null) {
            builder.claim("role", role);
        }

        return builder.signWith(secretKey, Jwts.SIG.HS256).compact();
    }

    public String getTokenUserId(String token) {
        return getClaims(token).get("userId", String.class);
    }

    public String getEmailFromToken(String token) {
        return getClaims(token).getSubject();
    }

    public Long getRoleFromToken(String token) {
        return getClaims(token).get("role", Long.class);
    }

    public String getTokenType(String token) {
        return getClaims(token).get("type", String.class);
    }

    public long getAccessTokenExpiresIn() {
        return accessTokenExpiration / 1000;
    }

    public long getRefreshTokenExpiration() {
        return refreshTokenExpiration / 1000;
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    /**
     * 토큰 유효성 검증
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token);
            return true;
        } catch (SecurityException | SignatureException e) {
            log.error("Invalid JWT signature -> {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token -> {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("Expired JWT token -> {}", e.getMessage());
            throw new ExpiredTokenException();
        } catch (UnsupportedJwtException e) {
            log.error("Unsupported JWT token -> {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty -> {}", e.getMessage());
        }

        throw new InvalidTokenException();
    }

    /**
     * Access 검증
     */
    public boolean validateAccessToken(String token) {
        validateToken(token);

        try {
            String tokenType = getTokenType(token);
            if (!"access".equals(tokenType)) {
                log.warn("Token is not a Access Token. Type -> {}", tokenType);
                return false;
            }
            return true;
        } catch (Exception e) {
            log.error("Error validating access token type");
            return false;
        }
    }

    /**
     * Refresh 검증
     */
    public boolean validateRefreshToken(String token) {
        validateToken(token);

        try {
            String tokenType = getTokenType(token);
            if (!"refresh".equals(tokenType)) {
                log.warn("Token is not a Refresh Token. Type -> {}", tokenType);
                return false;
            }
            return true;
        } catch (Exception e) {
            log.error("Error validating refresh token type");
            return false;
        }
    }

}
