package com.jendo.app.domain.notification.service;

import com.jendo.app.domain.notification.entity.ScheduledNotification;
import com.jendo.app.domain.notification.repository.DeviceTokenRepository;
import com.jendo.app.domain.notification.repository.ScheduledNotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationSchedulerService {

    private final ScheduledNotificationRepository scheduledNotificationRepository;
    private final DeviceTokenRepository deviceTokenRepository;
    private final FirebaseNotificationService firebaseNotificationService;

    @Scheduled(fixedRate = 60000) // Run every minute
    @Transactional
    public void sendPendingNotifications() {
        LocalDateTime now = LocalDateTime.now();
        List<ScheduledNotification> pendingNotifications =
                scheduledNotificationRepository.findByScheduledForBeforeAndSentFalse(now);

        log.info("Found {} pending notifications to send", pendingNotifications.size());

        for (ScheduledNotification notification : pendingNotifications) {
            try {
                sendScheduledNotification(notification);
                notification.setSent(true);
                notification.setSentAt(LocalDateTime.now());
                scheduledNotificationRepository.save(notification);
            } catch (Exception e) {
                log.error("Error sending notification ID: {}", notification.getId(), e);
            }
        }
    }

    private void sendScheduledNotification(ScheduledNotification notification) {
        List<String> tokens = deviceTokenRepository.findActiveTokensByUserId(notification.getUserId());
        if (tokens.isEmpty()) {
            log.warn("No active device tokens found for user: {}", notification.getUserId());
            return;
        }

        Map<String, String> data = new HashMap<>();
        data.put("type", notification.getType());
        data.put("notificationId", notification.getId().toString());

        if (notification.getAppointment() != null) {
            data.put("appointmentId", notification.getAppointment().getId().toString());
        }

        firebaseNotificationService.sendToTokens(
                tokens,
                notification.getTitle(),
                notification.getMessage(),
                data
        );
    }
}