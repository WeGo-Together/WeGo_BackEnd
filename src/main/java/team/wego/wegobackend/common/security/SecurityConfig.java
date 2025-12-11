package team.wego.wegobackend.common.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer.FrameOptionsConfig;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {

        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
            .authorizeHttpRequests((auth) -> auth
                    .requestMatchers(HttpMethod.GET, "/api/v1/users/*").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/v1/groups/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/v1/groups").permitAll()
                .requestMatchers(SecurityEndpoints.PUBLIC_PATTERNS).permitAll()
                .anyRequest().authenticated()
            );

        http
            .headers(headers -> headers.frameOptions(
                FrameOptionsConfig::sameOrigin))   // H2DB Console 활용을 위해 추가
            .csrf((auth) -> auth.disable())
            .formLogin((auth) -> auth.disable())
            .httpBasic((auth) -> auth.disable());

        http
            .sessionManagement(
                (session) -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    //TODO : CORS 설정 추가
}