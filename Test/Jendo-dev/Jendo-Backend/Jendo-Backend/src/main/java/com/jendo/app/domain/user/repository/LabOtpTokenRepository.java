package com.jendo.app.domain.user.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import com.jendo.app.domain.user.entity.LabOtpToken;

public interface LabOtpTokenRepository extends JpaRepository<LabOtpToken, Long> {
    Optional<LabOtpToken> findByUserEmailAndOtpAndExpireTimeAfter(String userEmail, String otp, LocalDateTime currentTime);
    Optional<LabOtpToken> findByUserEmail(String userEmail);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM LabOtpToken WHERE expireTime < :currentTime")
    void deleteExpiredTokens(LocalDateTime currentTime);
    
    @Modifying
    @Transactional
    void deleteByUserEmail(String userEmail);
}
