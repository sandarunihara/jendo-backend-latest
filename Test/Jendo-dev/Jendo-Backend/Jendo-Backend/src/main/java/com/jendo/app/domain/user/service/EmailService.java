package com.jendo.app.domain.user.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EmailService {
    @Autowired
    private JavaMailSender mailSender;
    
    @Value("${spring.mail.from:noreply@jendo.com}")
    private String fromAddress;

    public void sendOtpEmail(String to, String otp) {
        log.info("=== DEV OTP for {} : {} ===", to, otp);
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(to);
            message.setSubject("Jendo - Your OTP Code");
            message.setText(buildOtpEmailBody(otp, "verify your email"));
            mailSender.send(message);
            log.info("OTP email sent to: {}", to);
        } catch (Exception e) {
            log.warn("Email not configured - using logged OTP for development. OTP for {}: {}", to, otp);
        }
    }
    
    public void sendPasswordResetOtp(String to, String otp) {
        log.info("=== DEV PASSWORD RESET OTP for {} : {} ===", to, otp);
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(to);
            message.setSubject("Jendo - Password Reset OTP");
            message.setText(buildOtpEmailBody(otp, "reset your password"));
            mailSender.send(message);
            log.info("Password reset OTP email sent to: {}", to);
        } catch (Exception e) {
            log.warn("Email not configured - using logged OTP for development. Password reset OTP for {}: {}", to, otp);
        }
    }
    
    public void sendWelcomeEmail(String to, String firstName) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(to);
            message.setSubject("Welcome to Jendo!");
            message.setText(String.format(
                "Hello %s,\n\n" +
                "Welcome to Jendo! Your account has been created successfully.\n\n" +
                "Thank you for choosing Jendo for your cardiovascular health monitoring.\n\n" +
                "Best regards,\n" +
                "The Jendo Team",
                firstName
            ));
            mailSender.send(message);
            log.info("Welcome email sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send welcome email to: {}", to, e);
        }
    }
    
    private String buildOtpEmailBody(String otp, String purpose) {
        return String.format(
            "Hello,\n\n" +
            "Your OTP code to %s is: %s\n\n" +
            "This code will expire in 10 minutes.\n\n" +
            "If you did not request this code, please ignore this email.\n\n" +
            "Best regards,\n" +
            "The Jendo Team",
            purpose, otp
        );
    }
}
