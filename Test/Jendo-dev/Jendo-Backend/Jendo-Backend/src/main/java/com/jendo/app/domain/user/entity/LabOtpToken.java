package com.jendo.app.domain.user.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "lab_otp_token")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LabOtpToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "otp", nullable = false, length = 10)
    private String otp;
    
    @Column(name = "user_email", nullable = false, length = 255)
    private String userEmail;
    
    @Column(name = "expire_time", nullable = false)
    private LocalDateTime expireTime;
    
    @Column(name = "lab_assistant_id")
    private Long labAssistantId;
}
