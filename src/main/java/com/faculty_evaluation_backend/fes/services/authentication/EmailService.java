package com.faculty_evaluation_backend.fes.services.authentication;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender javaMailSender;

    public void sendAccessCodeEmail(
            String to,
            String accessCode,
            Instant expiresAt
    ) {

        try {

            MimeMessage message = javaMailSender.createMimeMessage();

            MimeMessageHelper helper =
                    new MimeMessageHelper(message, true, "UTF-8");

            String formattedExpiry =
                    DateTimeFormatter.ofPattern("MMMM dd, yyyy hh:mm a")
                            .withZone(ZoneId.systemDefault())
                            .format(expiresAt);

            String htmlContent = """
                    <!DOCTYPE html>
                    <html>
                    <head>
                        <meta charset="UTF-8">
                    </head>

                    <body style="
                        margin: 0;
                        padding: 0;
                        background-color: #f8f9fa;
                        font-family: Arial, sans-serif;
                        color: #000101;
                    ">

                    <table width="100%%" cellpadding="0" cellspacing="0" border="0" style="background-color: #f8f9fa; padding: 40px 0;">
                        <tr>
                            <td align="center">

                                <table width="600" cellpadding="0" cellspacing="0" border="0" style="
                                    background-color: #ffffff;
                                    border: 1px solid #e9ecef;
                                    border-radius: 12px;
                                    overflow: hidden;
                                ">

                                    <!-- Header -->
                                    <tr>
                                        <td style="
                                            background-color: #186443;
                                            padding: 30px;
                                            text-align: center;
                                        ">
                                            <h1 style="
                                                margin: 0;
                                                color: #ffffff;
                                                font-size: 28px;
                                                font-weight: bold;
                                            ">
                                                Faculty Evaluation System
                                            </h1>

                                            <p style="
                                                margin-top: 10px;
                                                color: #e5d413;
                                                font-size: 14px;
                                            ">
                                                Secure Student Access Code
                                            </p>
                                        </td>
                                    </tr>

                                    <!-- Body -->
                                    <tr>
                                        <td style="padding: 40px;">

                                            <p style="
                                                margin-top: 0;
                                                font-size: 16px;
                                                line-height: 1.6;
                                                color: #000101;
                                            ">
                                                Hello Student,
                                            </p>

                                            <p style="
                                                font-size: 16px;
                                                line-height: 1.6;
                                                color: #000101;
                                            ">
                                                Your access code for the Faculty Evaluation System has been generated successfully.
                                            </p>

                                            <!-- Access Code Box -->
                                            <table width="100%%" cellpadding="0" cellspacing="0" border="0">
                                                <tr>
                                                    <td align="center">
                                                        <div style="
                                                            background-color: #eef3f1;
                                                            border: 2px dashed #186443;
                                                            border-radius: 10px;
                                                            padding: 25px;
                                                            margin: 30px 0;
                                                            text-align: center;
                                                        ">
                                                            <p style="
                                                                margin: 0;
                                                                font-size: 14px;
                                                                color: #6c757d;
                                                                letter-spacing: 1px;
                                                            ">
                                                                YOUR ACCESS CODE
                                                            </p>

                                                            <h2 style="
                                                                margin: 15px 0 0 0;
                                                                color: #186443;
                                                                font-size: 36px;
                                                                letter-spacing: 6px;
                                                            ">
                                                                %s
                                                            </h2>
                                                        </div>
                                                    </td>
                                                </tr>
                                            </table>

                                            <!-- Expiration -->
                                            <div style="
                                                background-color: #fff8e1;
                                                border-left: 5px solid #cf6a33;
                                                padding: 16px;
                                                border-radius: 6px;
                                                margin-bottom: 30px;
                                            ">
                                                <p style="
                                                    margin: 0;
                                                    color: #000101;
                                                    font-size: 14px;
                                                    line-height: 1.5;
                                                ">
                                                    This access code will expire on:
                                                    <strong>%s</strong>
                                                </p>
                                            </div>

                                            <p style="
                                                font-size: 15px;
                                                line-height: 1.6;
                                                color: #000101;
                                            ">
                                                Please do not share this code with anyone.
                                            </p>

                                            <p style="
                                                font-size: 15px;
                                                line-height: 1.6;
                                                color: #000101;
                                            ">
                                                Thank you.
                                            </p>

                                        </td>
                                    </tr>

                                    <!-- Footer -->
                                    <tr>
                                        <td style="
                                            background-color: #eef3f1;
                                            padding: 20px;
                                            text-align: center;
                                            border-top: 1px solid #e9ecef;
                                        ">
                                            <p style="
                                                margin: 0;
                                                font-size: 13px;
                                                color: #6c757d;
                                            ">
                                                CHMSU Faculty Evaluation System
                                            </p>

                                            <p style="
                                                margin-top: 8px;
                                                font-size: 12px;
                                                color: #6c757d;
                                            ">
                                                This is an automated email. Please do not reply.
                                            </p>
                                        </td>
                                    </tr>

                                </table>

                            </td>
                        </tr>
                    </table>

                    </body>
                    </html>
                    """.formatted(accessCode, formattedExpiry);

            helper.setTo(to);
            helper.setSubject("Faculty Evaluation Access Code");
            helper.setText(htmlContent, true);

            javaMailSender.send(message);

        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email", e);
        }
    }
}