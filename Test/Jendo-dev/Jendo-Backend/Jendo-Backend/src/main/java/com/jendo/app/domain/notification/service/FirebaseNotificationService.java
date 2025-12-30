package com.jendo.app.domain.notification.service;

import com.google.firebase.messaging.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class FirebaseNotificationService {

    public void sendToTokens(List<String> tokens, String title, String body, Map<String, String> data) {
        if (tokens == null || tokens.isEmpty()) {
            log.warn("No tokens provided for notification");
            return;
        }

        try {
            // Filter valid tokens
            tokens = tokens.stream()
                    .filter(t -> t != null && !t.trim().isEmpty())
                    .toList();

            if (tokens.isEmpty()) {
                log.warn("No valid tokens after filtering");
                return;
            }

            log.info("Sending FCM notification to {} tokens", tokens.size());

            int successCount = 0;
            int failureCount = 0;

            // ✅ Send to each token individually (avoids /batch endpoint)
            for (String token : tokens) {
                try {
                    Message message = Message.builder()
                            .setToken(token)
                            .setNotification(Notification.builder()
                                    .setTitle(title)
                                    .setBody(body)
                                    .build())
                            .putAllData(data == null ? Map.of() : data)
                            .setAndroidConfig(AndroidConfig.builder()
                                    .setPriority(AndroidConfig.Priority.HIGH)
                                    .setNotification(AndroidNotification.builder()
                                            .setSound("default")
                                            .setChannelId("default")
                                            .build())
                                    .build())
                            .setApnsConfig(ApnsConfig.builder()
                                    .setAps(Aps.builder()
                                            .setSound("default")
                                            .setBadge(1)
                                            .build())
                                    .build())
                            .build();

                    // ✅ Use send() instead of sendMulticast()
                    String response = FirebaseMessaging.getInstance().send(message);
                    successCount++;
                    log.debug("Sent to token successfully: {}", response);

                } catch (FirebaseMessagingException e) {
                    failureCount++;
                    log.error("Failed to send to token: {} - Error: {}",
                            token.substring(0, Math.min(20, token.length())) + "...",
                            e.getMessage());
                }
            }

            log.info("✅ FCM sent: success={}, failure={}", successCount, failureCount);

        } catch (Exception e) {
            log.error("❌ Unexpected error sending FCM notification", e);
        }
    }
}