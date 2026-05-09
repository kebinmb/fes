package com.faculty_evaluation_backend.fes.services.authentication;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import lombok.RequiredArgsConstructor;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;

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

            MimeMessage message =
                    javaMailSender.createMimeMessage();

            MimeMessageHelper helper =
                    new MimeMessageHelper(
                            message,
                            true,
                            "UTF-8"
                    );

            String formattedExpiry =
                    DateTimeFormatter
                            .ofPattern("MMMM dd, yyyy hh:mm a")
                            .withZone(ZoneId.systemDefault())
                            .format(expiresAt);

            String htmlContent = """
                    <!DOCTYPE html>
                    <html lang="en">
                    <head>
                        <meta charset="UTF-8">
                        <meta name="viewport" content="width=device-width, initial-scale=1.0">
                        <title>Faculty Evaluation Access Code</title>
                    </head>
                    
                    <body style="
                        margin: 0;
                        padding: 0;
                        background-color: #f4f7fb;
                        font-family: Arial, Helvetica, sans-serif;
                        color: #1f2937;
                    ">
                    
                    <table
                        width="100%%"
                        cellpadding="0"
                        cellspacing="0"
                        border="0"
                        style="
                            background: linear-gradient(
                                135deg,
                                #eef6f1 0%%,
                                #f4f7fb 100%%
                            );
                            padding: 40px 20px;
                        "
                    >
                    
                    <tr>
                    <td align="center">
                    
                    <!-- MAIN CONTAINER -->
                    <table
                        width="640"
                        cellpadding="0"
                        cellspacing="0"
                        border="0"
                        style="
                            background-color: #ffffff;
                            border-radius: 22px;
                            overflow: hidden;
                            box-shadow:
                                0 10px 35px rgba(0,0,0,0.08);
                        "
                    >
                    
                    <!-- TOP ACCENT -->
                    <tr>
                    <td style="
                        height: 8px;
                        background: linear-gradient(
                            90deg,
                            #186443 0%%,
                            #2c8f61 50%%,
                            #e5d413 100%%
                        );
                    ">
                    </td>
                    </tr>
                    
                    <!-- HEADER -->
                    <tr>
                    <td style="
                        padding: 42px 50px 30px 50px;
                        background-color: #ffffff;
                    ">
                    
                    <table width="100%%" cellpadding="0" cellspacing="0">
                    <tr>
                    
                    <td align="left">
                    
                    <div style="
                        width: 64px;
                        height: 64px;
                        border-radius: 18px;
                        background-color: #186443;
                        text-align: center;
                        line-height: 64px;
                        font-size: 30px;
                        color: white;
                        font-weight: bold;
                    ">
                        FE
                    </div>
                    
                    </td>
                    
                    <td align="right">
                    
                    <p style="
                        margin: 0;
                        font-size: 13px;
                        color: #6b7280;
                        letter-spacing: 1px;
                    ">
                        SECURE ACCESS PORTAL
                    </p>
                    
                    </td>
                    
                    </tr>
                    </table>
                    
                    <h1 style="
                        margin-top: 30px;
                        margin-bottom: 10px;
                        font-size: 34px;
                        color: #111827;
                        line-height: 1.2;
                    ">
                        Faculty Evaluation System
                    </h1>
                    
                    <p style="
                        margin: 0;
                        font-size: 16px;
                        color: #6b7280;
                        line-height: 1.7;
                    ">
                        Your secure one-time access code has been generated successfully.
                    </p>
                    
                    </td>
                    </tr>
                    
                    <!-- BODY -->
                    <tr>
                    <td style="padding: 0 50px 40px 50px;">
                    
                    <!-- GREETING -->
                    <p style="
                        margin-top: 0;
                        margin-bottom: 18px;
                        font-size: 16px;
                        color: #374151;
                        line-height: 1.8;
                    ">
                        Hello Student,
                    </p>
                    
                    <p style="
                        margin-top: 0;
                        margin-bottom: 28px;
                        font-size: 16px;
                        color: #4b5563;
                        line-height: 1.8;
                    ">
                        Please use the secure verification code below to continue accessing the
                        Faculty Evaluation System platform.
                    </p>
                    
                    <!-- ACCESS CODE CARD -->
                    <div style="
                        background:
                            linear-gradient(
                                135deg,
                                #186443 0%%,
                                #237552 100%%
                            );
                        border-radius: 20px;
                        padding: 35px 30px;
                        text-align: center;
                        margin-bottom: 30px;
                    ">
                    
                    <p style="
                        margin: 0;
                        color: rgba(255,255,255,0.75);
                        font-size: 13px;
                        letter-spacing: 2px;
                    ">
                        YOUR ACCESS CODE
                    </p>
                    
                    <h2 style="
                        margin-top: 18px;
                        margin-bottom: 0;
                        color: #ffffff;
                        font-size: 42px;
                        letter-spacing: 10px;
                        font-weight: bold;
                    ">
                        %s
                    </h2>
                    
                    </div>
                    
                    <!-- EXPIRATION CARD -->
                    <table
                        width="100%%"
                        cellpadding="0"
                        cellspacing="0"
                        border="0"
                        style="
                            background-color: #fff8e8;
                            border: 1px solid #f3d27a;
                            border-radius: 14px;
                            margin-bottom: 28px;
                        "
                    >
                    
                    <tr>
                    <td style="padding: 18px 22px;">
                    
                    <p style="
                        margin: 0;
                        color: #92400e;
                        font-size: 15px;
                        line-height: 1.7;
                    ">
                        ⏳ This access code will expire on:
                        <strong>%s</strong>
                    </p>
                    
                    </td>
                    </tr>
                    
                    </table>
                    
                    <!-- SECURITY NOTE -->
                    <div style="
                        background-color: #f9fafb;
                        border-radius: 14px;
                        padding: 22px;
                        border: 1px solid #e5e7eb;
                    ">
                    
                    <p style="
                        margin-top: 0;
                        margin-bottom: 12px;
                        font-size: 15px;
                        color: #111827;
                        font-weight: bold;
                    ">
                        Security Reminder
                    </p>
                    
                    <p style="
                        margin: 0;
                        font-size: 14px;
                        color: #6b7280;
                        line-height: 1.8;
                    ">
                        Never share your access code with anyone.
                        University administrators will never ask for your code through email or chat.
                    </p>
                    
                    </div>
                    
                    <!-- THANK YOU -->
                    <p style="
                        margin-top: 35px;
                        margin-bottom: 0;
                        font-size: 15px;
                        color: #4b5563;
                        line-height: 1.8;
                    ">
                        Thank you for using the
                        <strong>CHMSU Faculty Evaluation System</strong>.
                    </p>
                    
                    </td>
                    </tr>
                    
                    <!-- FOOTER -->
                    <tr>
                    <td style="
                        background-color: #f9fafb;
                        padding: 30px;
                        text-align: center;
                        border-top: 1px solid #e5e7eb;
                    ">
                    
                    <p style="
                        margin: 0;
                        font-size: 15px;
                        font-weight: bold;
                        color: #186443;
                    ">
                        Carlos Hilado Memorial State University
                    </p>
                    
                    <p style="
                        margin-top: 10px;
                        margin-bottom: 0;
                        font-size: 13px;
                        color: #6b7280;
                        line-height: 1.8;
                    ">
                        Faculty Evaluation System
                    </p>
                    
                    <p style="
                        margin-top: 10px;
                        margin-bottom: 0;
                        font-size: 12px;
                        color: #9ca3af;
                        line-height: 1.8;
                    ">
                        This is an automated message.
                        Please do not reply to this email.
                    </p>
                    
                    </td>
                    </tr>
                    
                    </table>
                    
                    <!-- END MAIN CONTAINER -->
                    
                    </td>
                    </tr>
                    
                    </table>
                    
                    </body>
                    </html>
                    """.formatted(accessCode, formattedExpiry);

            /* =========================================
               CUSTOM SENDER NAME
            ========================================= */

            helper.setFrom(
                    "your-email@gmail.com",
                    "Carlos Hilado Memorial State University - Faculty Evaluation System"
            );

            helper.setTo(to);

            helper.setSubject(
                    "Faculty Evaluation Access Code"
            );

            helper.setText(
                    htmlContent,
                    true
            );

            javaMailSender.send(message);

        } catch (
                MessagingException |
                UnsupportedEncodingException e
        ) {

            throw new RuntimeException(
                    "Failed to send email",
                    e
            );
        }
    }
}