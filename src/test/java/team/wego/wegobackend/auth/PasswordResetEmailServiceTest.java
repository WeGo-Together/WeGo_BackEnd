package team.wego.wegobackend.auth;

import static org.assertj.core.api.Assertions.assertThatCode;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.boot.autoconfigure.mail.MailSenderValidatorAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import team.wego.wegobackend.auth.application.PasswordResetEmailService;
import team.wego.wegobackend.common.config.AsyncConfig;

/**
 * 실제 SMTP 서버로 이메일 발송을 검증하는 통합 테스트.
 *
 * <p>실행 전 .env 파일에 MAIL_USERNAME, MAIL_PASSWORD가 설정되어 있어야 합니다.
 * 테스트 이메일은 MAIL_USERNAME 주소 본인에게 발송됩니다.
 */
@SpringBootTest(
        classes = {PasswordResetEmailService.class, AsyncConfig.class}
)
@ImportAutoConfiguration({
        MailSenderAutoConfiguration.class,
        MailSenderValidatorAutoConfiguration.class
})
class PasswordResetEmailServiceTest {

    @Autowired
    private PasswordResetEmailService emailService;

    @Value("${spring.mail.username}")
    private String mailUsername;

    @Test
    @DisplayName("실제 SMTP 서버로 비밀번호 재설정 이메일 발송 검증")
    void sendPasswordResetEmail_실제_발송() {
        String testResetUrl = "https://wego.monster/password-reset?validationValue=test-token-1234";

        assertThatCode(() ->
                emailService.sendPasswordResetEmail(mailUsername, testResetUrl)
        ).doesNotThrowAnyException();

        System.out.println("✅ 이메일 발송 완료. 수신함 확인: " + mailUsername);
    }
}