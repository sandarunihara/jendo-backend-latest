package com.jendo.app.domain.notification.repository;

import com.jendo.app.domain.notification.entity.DeviceToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceTokenRepository extends JpaRepository<DeviceToken, Long> {

    @Query("SELECT dt.fcmToken FROM DeviceToken dt WHERE dt.user.id = :userId AND dt.isActive = true")
    List<String> findActiveTokensByUserId(Long userId);

    Optional<DeviceToken> findByFcmToken(String fcmToken);
}