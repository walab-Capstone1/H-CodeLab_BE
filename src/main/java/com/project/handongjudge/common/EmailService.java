package com.project.handongjudge.common;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

/**
 * 이메일 발송 서비스 (Gmail SMTP)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    /**
     * 비밀번호 재설정 이메일 발송
     *
     * @param toEmail  수신자 이메일
     * @param token    재설정 토큰 (UUID)
     */
    public void sendPasswordResetEmail(String toEmail, String token) {
        String resetLink = frontendUrl + "/reset-password?token=" + token;
        String subject = "[H-CodeLab] 비밀번호 재설정 안내";
        String htmlContent = buildPasswordResetHtml(resetLink);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            mailSender.send(message);
            log.info("비밀번호 재설정 이메일 발송 완료: {}", toEmail);
        } catch (MessagingException e) {
            log.error("이메일 발송 실패: {}", toEmail, e);
            throw new RuntimeException("이메일 발송에 실패했습니다. 잠시 후 다시 시도해주세요.");
        }
    }

    private String buildPasswordResetHtml(String resetLink) {
        return "<!DOCTYPE html>" +
            "<html lang=\"ko\">" +
            "<head>" +
            "  <meta charset=\"UTF-8\">" +
            "  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">" +
            "</head>" +
            "<body style=\"margin:0; padding:0; background:#f0f4ff; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;\">" +
            "  <table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"background:#f0f4ff; padding: 40px 0;\">" +
            "    <tr><td align=\"center\">" +
            "      <table width=\"560\" cellpadding=\"0\" cellspacing=\"0\" style=\"background:#ffffff; border-radius:16px; overflow:hidden; box-shadow: 0 8px 30px rgba(102,126,234,0.15);\">" +
            "        <tr>" +
            "          <td style=\"background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); padding: 36px 40px; text-align:center;\">" +
            "            <h1 style=\"color:#ffffff; font-size:24px; margin:0; font-weight:700;\">H-CodeLab</h1>" +
            "            <p style=\"color:rgba(255,255,255,0.85); margin:8px 0 0; font-size:14px;\">비밀번호 재설정 안내</p>" +
            "          </td>" +
            "        </tr>" +
            "        <tr>" +
            "          <td style=\"padding: 40px 40px 32px;\">" +
            "            <p style=\"color:#1a1a2e; font-size:16px; margin:0 0 16px; font-weight:600;\">안녕하세요! 👋</p>" +
            "            <p style=\"color:#4b5563; font-size:15px; line-height:1.7; margin:0 0 24px;\">" +
            "              H-CodeLab 계정의 비밀번호 재설정 요청이 접수되었습니다.<br>" +
            "              아래 버튼을 클릭하면 새로운 비밀번호를 설정할 수 있습니다." +
            "            </p>" +
            "            <div style=\"text-align:center; margin: 32px 0;\">" +
            "              <a href=\"" + resetLink + "\" style=\"display:inline-block; padding:16px 40px; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color:#ffffff; text-decoration:none; border-radius:10px; font-size:16px; font-weight:600; box-shadow: 0 4px 15px rgba(102,126,234,0.4);\">" +
            "                비밀번호 재설정하기" +
            "              </a>" +
            "            </div>" +
            "            <div style=\"background:#f8f9ff; border-radius:10px; padding:16px 20px; margin: 0 0 24px;\">" +
            "              <p style=\"color:#6b7280; font-size:13px; margin:0; line-height:1.6;\">" +
            "                ⏰ 이 링크는 <strong style=\"color:#667eea;\">15분</strong> 후 만료됩니다.<br>" +
            "                🔒 링크는 1회만 사용 가능합니다.<br>" +
            "                ❓ 본인이 요청하지 않은 경우 이 이메일을 무시해주세요." +
            "              </p>" +
            "            </div>" +
            "          </td>" +
            "        </tr>" +
            "        <tr>" +
            "          <td style=\"padding: 20px 40px 32px; border-top: 1px solid #f0f0f0; text-align:center;\">" +
            "            <p style=\"color:#9ca3af; font-size:12px; margin:0; line-height:1.6;\">" +
            "              이 이메일은 H-CodeLab 계정 보안을 위해 자동 발송되었습니다.<br>" +
            "              문의사항이 있으시면 관리자에게 문의해주세요." +
            "            </p>" +
            "          </td>" +
            "        </tr>" +
            "      </table>" +
            "    </td></tr>" +
            "  </table>" +
            "</body>" +
            "</html>";
    }
}
