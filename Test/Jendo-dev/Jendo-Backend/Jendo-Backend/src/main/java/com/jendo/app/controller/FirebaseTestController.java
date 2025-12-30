package com.jendo.app.controller;

import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/firebase-test")
@Slf4j
public class FirebaseTestController {

    @GetMapping("/status")
    public ResponseEntity<?> checkStatus() {
        try {
            boolean isInitialized = !FirebaseApp.getApps().isEmpty();
            return ResponseEntity.ok(new FirebaseStatusDto(
                    isInitialized,
                    isInitialized ? "Firebase Admin SDK is initialized" : "Firebase not initialized"
            ));
        } catch (Exception e) {
            log.error("Firebase status check failed", e);
            return ResponseEntity.status(500).body(new FirebaseStatusDto(
                    false,
                    "Error: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/test-send/{token}")
    public ResponseEntity<?> testSendNotification(@PathVariable String token) {
        try {
            Message message = Message.builder()
                    .setNotification(Notification.builder()
                            .setTitle("Test Notification")
                            .setBody("This is a test from backend")
                            .build())
                    .setToken(token)
                    .build();

            String response = FirebaseMessaging.getInstance().send(message);
            log.info("Test notification sent: {}", response);

            return ResponseEntity.ok(new TestResponseDto(true, "Notification sent: " + response));
        } catch (Exception e) {
            log.error("Test notification failed", e);
            return ResponseEntity.status(500).body(new TestResponseDto(false, "Error: " + e.getMessage()));
        }
    }

    record FirebaseStatusDto(boolean initialized, String message) {}
    record TestResponseDto(boolean success, String message) {}
}