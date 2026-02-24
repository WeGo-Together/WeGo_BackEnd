package team.wego.wegobackend.common.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.cors.CorsUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import team.wego.wegobackend.auth.exception.UserNotFoundException;
import team.wego.wegobackend.common.exception.AppErrorCode;
import team.wego.wegobackend.common.exception.AppException;
import team.wego.wegobackend.common.response.ErrorResponse;
import team.wego.wegobackend.common.security.exception.DuplicateSessionException;
import team.wego.wegobackend.common.security.exception.ExpiredTokenException;
import team.wego.wegobackend.common.security.exception.InvalidTokenException;
import team.wego.wegobackend.common.security.jwt.JwtTokenProvider;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService userDetailsService;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    private final ObjectMapper objectMapper;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return Arrays.stream(SecurityEndpoints.PUBLIC_PATTERNS)
                .anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain) throws IOException {

        try {

            String jwt = getJwtFromRequest(request);

            if (StringUtils.hasText(jwt) && jwtTokenProvider.validateAccessToken(jwt)) {


                String email = jwtTokenProvider.getEmailFromToken(jwt);

                CustomUserDetails userDetails = (CustomUserDetails) userDetailsService.loadUserByUsername(
                        email);

                // 동시 로그인 제한 : DB의 세션값과 비교
                String currentSessionid = userDetails.getCurrentSessionid();
                String tokenSid = jwtTokenProvider.getSidFromToken(jwt);

                if (!tokenSid.equals(currentSessionid)) {
                    throw new DuplicateSessionException();
                }

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());

                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request));

                // 6. SecurityContext에 저장
                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.debug("JWT 인증 성공: {}", email);

            } else {
                if (!isPublicEndpoint(request)) {
                    throw new InvalidTokenException();
                }
            }

            filterChain.doFilter(request, response);


        } catch (ExpiredTokenException | InvalidTokenException | DuplicateSessionException
                 | UserNotFoundException e) {
            sendJsonError(response, e);
        } catch (Exception e) {
            log.error("JWT 필터 처리 중 예외 발생", e);
            sendJsonError(response, e);
        }

    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        //SSE 연결을 위한 추출 전략 추가
        String tokenFromQuery = request.getParameter("accessToken");
        if (StringUtils.hasText(tokenFromQuery)) {
            return tokenFromQuery;
        }
        return null;
    }

    /**
     * Servlet 예외 처리 메서드
     */
    private void sendJsonError(HttpServletResponse response, Exception e) throws IOException {
        if (response.isCommitted()) {
            log.warn("Response already committed, cannot send error: {}", e.getMessage());
            return;
        }

        AppErrorCode errorCode = resolveErrorCode(e);

        response.setStatus(errorCode.getHttpStatus().value());
        response.setContentType("application/json;charset=UTF-8");

        ErrorResponse errorResponse = ErrorResponse.of(
                errorCode.getHttpStatus(),
                errorCode.getMessageTemplate(),
                errorCode.name()
        );

        objectMapper.writeValue(response.getWriter(), errorResponse);
    }

    private AppErrorCode resolveErrorCode(Exception e) {
        if (e instanceof ExpiredTokenException) return AppErrorCode.EXPIRED_TOKEN;
        if (e instanceof InvalidTokenException) return AppErrorCode.INVALID_TOKEN;
        if (e instanceof DuplicateSessionException) return AppErrorCode.DUPLICATE_LOGIN;
        if (e instanceof AppException appEx && appEx.getErrorCode() instanceof AppErrorCode code) return code;
        return AppErrorCode.INTERNAL_SERVER_ERROR;
    }

    /**
     * Public 엔드포인트 확인
     */
    private boolean isPublicEndpoint(HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();

        if (CorsUtils.isPreFlightRequest(request)) {
            return true;
        }

        //TODO : PUBLIC_PATTERNS 관리 포인트 개선 필요 (메서드까지 관리 확장)
        if (pathMatcher.match("/api/v1/users/me", path)) {
            return false;
        }

        if ("GET".equals(method) && pathMatcher.match("/api/v*/groups/me", path)) {
            return false;
        }

        if ("GET".equals(method) && pathMatcher.match("/actuator/**", path)) {
            return false;
        }

        if ("GET".equals(method) && pathMatcher.match("/api/v*/groups/me", path)) {
            return false;
        }

        if ("GET".equals(method) && pathMatcher.match("/api/v1/users/**", path)) {
            return true;
        }

        if ("GET".equals(method) && pathMatcher.match("/api/v1/groups/**", path)) {
            return true;
        }

        if ("GET".equals(method) && pathMatcher.match("/api/v1/group", path)) {
            return true;
        }

        if ("GET".equals(method) && pathMatcher.match("/api/v2/groups/**", path)) {
            return true;
        }

        if ("GET".equals(method) && pathMatcher.match("/api/v2/group", path)) {
            return true;
        }

        // SecurityEndpoints.PUBLIC_PATTERNS 체크
        return Arrays.stream(SecurityEndpoints.PUBLIC_PATTERNS)
                .anyMatch(pattern -> pathMatcher.match(pattern, path));
    }
}