package team.wego.wegobackend.common.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;

@Component
@RequiredArgsConstructor
@Slf4j
public class ApplicationStartupLogger {

    private final Environment env;
    private final DataSource dataSource;

    @EventListener(ApplicationReadyEvent.class)
    public void logApplicationStartup() {
        log.info("=".repeat(80));
        log.info("APPLICATION STARTED SUCCESSFULLY");
        log.info("=".repeat(80));

        // 프로파일 정보
        logProfiles();

        // 데이터베이스 정보
        logDatabaseInfo();

        // JPA 설정
        logJpaSettings();

        // JWT 설정
        logJwtSettings();

        // 서버 정보
        logServerInfo();

        log.info("=".repeat(80));
    }

    private void logProfiles() {
        String[] activeProfiles = env.getActiveProfiles();
        String[] defaultProfiles = env.getDefaultProfiles();

        log.info("📋 ACTIVE PROFILES: {}",
            activeProfiles.length > 0 ? String.join(", ", activeProfiles) : "none");

        if (defaultProfiles.length > 0) {
            log.info("📋 DEFAULT PROFILES: {}", String.join(", ", defaultProfiles));
        }
    }

    private void logDatabaseInfo() {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();

            log.info("🗄️  DATABASE INFORMATION:");
            log.info("   - Database Type: {} {}",
                metaData.getDatabaseProductName(),
                metaData.getDatabaseProductVersion()
            );
            log.info("   - JDBC Driver: {} {}",
                metaData.getDriverName(),
                metaData.getDriverVersion()
            );
            log.info("   - Connection URL: {}",
                maskPassword(metaData.getURL())
            );
            log.info("   - Username: {}", metaData.getUserName());
            log.info("   - Max Connections: {}",
                env.getProperty("spring.datasource.hikari.maximum-pool-size", "10")
            );

        } catch (Exception e) {
            log.error("❌ Failed to retrieve database information", e);
        }
    }

    private void logJpaSettings() {
        log.info("🔧 JPA SETTINGS:");
        log.info("   - DDL Auto: {}",
            env.getProperty("spring.jpa.hibernate.ddl-auto", "none")
        );
        log.info("   - Show SQL: {}",
            env.getProperty("spring.jpa.show-sql", "false")
        );
        log.info("   - Format SQL: {}",
            env.getProperty("spring.jpa.properties.hibernate.format_sql", "false")
        );
        log.info("   - Database Platform: {}",
            env.getProperty("spring.jpa.database-platform", "auto-detected")
        );
    }

    private void logJwtSettings() {
        log.info("🔐 JWT SETTINGS:");

        String secret = env.getProperty("jwt.secret");
        if (secret != null) {
            log.info("   - Secret Key: {}... (length: {})",
                secret.substring(0, Math.min(10, secret.length())),
                secret.length()
            );
        } else {
            log.warn("   - Secret Key: NOT CONFIGURED!");
        }

        String expiration = env.getProperty("jwt.expiration");
        if (expiration != null) {
            long expirationMs = Long.parseLong(expiration);
            long hours = expirationMs / (1000 * 60 * 60);
            log.info("   - Token Expiration: {} ms ({} hours)", expirationMs, hours);
        } else {
            log.warn("   - Token Expiration: NOT CONFIGURED!");
        }
    }

    private void logServerInfo() {
        String port = env.getProperty("server.port", "8080");
        String contextPath = env.getProperty("server.servlet.context-path", "/");

        log.info("🚀 SERVER INFORMATION:");
        log.info("   - Port: {}", port);
        log.info("   - Context Path: {}", contextPath);
        log.info("   - Base URL: http://localhost:{}{}", port, contextPath);

        // H2 Console 정보
        if ("true".equals(env.getProperty("spring.h2.console.enabled"))) {
            String h2Path = env.getProperty("spring.h2.console.path", "/h2-console");
            log.info("   - H2 Console: http://localhost:{}{}", port, h2Path);
        }
    }

    private String maskPassword(String url) {
        // URL에서 비밀번호 마스킹
        return url.replaceAll("password=[^&;]+", "password=****");
    }
}