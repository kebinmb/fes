package com.faculty_evaluation_backend.fes.services.authentication;

import com.faculty_evaluation_backend.fes.config.email.EmailAccount;
import com.faculty_evaluation_backend.fes.config.email.EmailAccountPool;
import com.faculty_evaluation_backend.fes.config.email.EmailProperties;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.io.UnsupportedEncodingException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private static final String FROM_NAME = "CHMSU Faculty Evaluation System";

    private final EmailAccountPool emailAccountPool;
    private final EmailProperties emailProperties;

    public void sendAccessCodeEmail(String to, String accessCode, Instant expiresAt) {
        String formattedExpiry = DateTimeFormatter
                .ofPattern("MMMM dd, yyyy hh:mm a")
                .withZone(ZoneId.systemDefault())
                .format(expiresAt);

        String htmlContent = buildAccessCodeHtml(accessCode, formattedExpiry);
        String textContent = buildAccessCodeText(accessCode, formattedExpiry);

        Exception lastException = null;

        for (EmailAccount account : emailAccountPool.getAll()) {
            try {
                JavaMailSender sender = createSender(account);
                MimeMessage message = sender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

                helper.setFrom(account.username(), FROM_NAME);
                helper.setTo(to);
                helper.setSubject("Faculty Evaluation Access Code");
                helper.setText(textContent, htmlContent);

                sender.send(message);
                return;
            } catch (MessagingException | UnsupportedEncodingException e) {
                lastException = e;
                log.warn("Email sender failed for account {}", account.username(), e);
            } catch (Exception e) {
                lastException = e;
                log.warn("SMTP failed for account {}: {}", account.username(), e.getMessage(), e);
            }
        }

        throw new RuntimeException("All configured email accounts failed.", lastException);
    }

    private JavaMailSender createSender(EmailAccount account) {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();

        sender.setHost(emailProperties.getSmtp().getHost());
        sender.setPort(emailProperties.getSmtp().getPort());
        sender.setUsername(account.username());
        sender.setPassword(account.password());

        Properties props = sender.getJavaMailProperties();
        props.put("mail.smtp.auth", String.valueOf(emailProperties.getSmtp().isAuth()));
        props.put("mail.smtp.starttls.enable", String.valueOf(emailProperties.getSmtp().isStarttls()));
        props.put("mail.smtp.connectiontimeout", "10000");
        props.put("mail.smtp.timeout", "10000");
        props.put("mail.smtp.writetimeout", "10000");

        return sender;
    }

    private String buildAccessCodeText(String accessCode, String formattedExpiry) {
        return """
                CHMSU Faculty Evaluation System
                
                Your one-time access code is: %s
                
                This code expires on %s.
                
                Never share your access code. CHMSU staff will never ask for this code through email or chat.
                
                This is an automated message. Please do not reply.
                """.formatted(accessCode, formattedExpiry);
    }

    private String buildAccessCodeHtml(String accessCode, String formattedExpiry) {
        String safeAccessCode = HtmlUtils.htmlEscape(accessCode);
        String safeFormattedExpiry = HtmlUtils.htmlEscape(formattedExpiry);

        return """
                <!doctype html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <meta name="color-scheme" content="light">
                    <title>Faculty Evaluation Access Code</title>
                </head>
                <body style="margin:0; padding:0; background-color:#eef2f0; font-family:Arial, Helvetica, sans-serif; color:#17211d;">
                <div style="display:none; max-height:0; overflow:hidden; opacity:0;">
                    Your CHMSU Faculty Evaluation access code is %s.
                </div>
                <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" border="0" style="background-color:#eef2f0;">
                    <tr>
                        <td align="center" style="padding:32px 16px;">
                            <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" border="0" style="max-width:640px; background-color:#ffffff; border:1px solid #dce5df; border-radius:8px; overflow:hidden;">
                                <tr>
                                    <td style="height:6px; background-color:#186443;"></td>
                                </tr>
                                <tr>
                                    <td style="padding:28px 32px 20px 32px;">
                                        <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" border="0">
                                            <tr>
                                                <td align="left" style="vertical-align:middle;">
                                                    <div style="display:inline-block; padding:10px 12px; border:1px solid #c9ded2; border-radius:8px; color:#186443; font-size:13px; font-weight:700; letter-spacing:.08em;">
                                                        CHMSU FES
                                                    </div>
                                                </td>
                                                <td align="right" style="vertical-align:middle; color:#6b756f; font-size:12px; letter-spacing:.08em; text-transform:uppercase;">
                                                    Secure access code
                                                </td>
                                            </tr>
                                        </table>
                                    </td>
                                </tr>
                                <tr>
                                    <td style="padding:0 32px 24px 32px;">
                                        <h1 style="margin:0 0 10px 0; font-size:26px; line-height:1.25; color:#17211d; font-weight:700;">
                                            Faculty Evaluation System
                                        </h1>
                                        <p style="margin:0; font-size:15px; line-height:1.7; color:#4f5f56;">
                                            Use this one-time code to continue signing in to the CHMSU Faculty Evaluation System.
                                        </p>
                                    </td>
                                </tr>
                                <tr>
                                    <td style="padding:0 32px 28px 32px;">
                                        <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" border="0" style="background-color:#f6faf7; border:1px solid #cfe2d7; border-radius:8px;">
                                            <tr>
                                                <td align="center" style="padding:28px 20px;">
                                                    <div style="margin:0 0 12px 0; color:#607169; font-size:12px; font-weight:700; letter-spacing:.12em; text-transform:uppercase;">
                                                        Your access code
                                                    </div>
                                                    <div style="font-family:'Courier New', Courier, monospace; color:#103e2b; font-size:34px; line-height:1.2; font-weight:700; letter-spacing:.12em;">
                                                        %s
                                                    </div>
                                                    <div style="margin-top:12px; color:#6b756f; font-size:12px;">
                                                        Enter exactly as shown.
                                                    </div>
                                                </td>
                                            </tr>
                                        </table>
                                    </td>
                                </tr>
                                <tr>
                                    <td style="padding:0 32px 24px 32px;">
                                        <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" border="0" style="background-color:#fff9e8; border:1px solid #ead89a; border-radius:8px;">
                                            <tr>
                                                <td style="padding:16px 18px;">
                                                    <p style="margin:0; color:#745300; font-size:14px; line-height:1.6;">
                                                        <strong>Expires:</strong> %s
                                                    </p>
                                                </td>
                                            </tr>
                                        </table>
                                    </td>
                                </tr>
                                <tr>
                                    <td style="padding:0 32px 32px 32px;">
                                        <p style="margin:0 0 12px 0; color:#17211d; font-size:15px; font-weight:700;">
                                            Security reminder
                                        </p>
                                        <p style="margin:0; color:#4f5f56; font-size:14px; line-height:1.7;">
                                            Never share this code. CHMSU staff will never ask for your access code through email, chat, or phone.
                                        </p>
                                    </td>
                                </tr>
                                <tr>
                                    <td style="padding:22px 32px; background-color:#f7f9f8; border-top:1px solid #e1e8e4;">
                                        <p style="margin:0; color:#186443; font-size:14px; font-weight:700;">
                                            Carlos Hilado Memorial State University
                                        </p>
                                        <p style="margin:6px 0 0 0; color:#7b8780; font-size:12px; line-height:1.6;">
                                            Faculty Evaluation System. This is an automated message; please do not reply.
                                        </p>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                </table>
                </body>
                </html>
                """.formatted(safeAccessCode, safeAccessCode, safeFormattedExpiry);
    }
}
