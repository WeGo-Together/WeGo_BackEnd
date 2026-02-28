package team.wego.wegobackend.auth.application;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import team.wego.wegobackend.common.exception.AppErrorCode;
import team.wego.wegobackend.common.exception.AppException;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetEmailService {

    private final JavaMailSender mailSender;

    /**
     * 비밀번호 재설정 이메일을 발송합니다.
     *
     * @param toEmail  수신자 이메일
     * @param resetUrl 비밀번호 재설정 페이지 전체 URL (검증값 포함)
     */
    @Async("mailExecutor")
    public void sendPasswordResetEmail(String toEmail, String resetUrl) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("[WeGo] 비밀번호 재설정 안내");
            helper.setText(buildEmailBody(resetUrl), true);

            mailSender.send(message);
            log.info("비밀번호 재설정 이메일 발송 완료 -> {}", toEmail);

        } catch (MessagingException | MailException e) {
            log.error("비밀번호 재설정 이메일 발송 실패 -> {}", toEmail, e);
            throw new AppException(AppErrorCode.DEPENDENCY_FAILURE);
        }
    }

    private String buildEmailBody(String resetUrl) {
        return """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
                    <h2 style="color: #333;">비밀번호 재설정</h2>
                    <p>안녕하세요. WeGo입니다.</p>
                    <p>아래 버튼을 클릭하여 비밀번호를 재설정하세요.</p>
                    <p>
                        <a href="%s"
                           style="display: inline-block; padding: 12px 24px; background-color: #4F46E5;
                                  color: white; text-decoration: none; border-radius: 6px;">
                            비밀번호 재설정
                        </a>
                    </p>
                    <p style="color: #888; font-size: 13px;">
                        본 링크는 <strong>30분</strong> 동안만 유효합니다.<br>
                        본인이 요청하지 않은 경우 이 이메일을 무시하셔도 됩니다.
                    </p>
                </div>
                """.formatted(resetUrl);
    }
}