package com.jendo.app.controller;

import com.jendo.app.common.dto.ApiResponse;
import com.jendo.app.common.dto.PaginationResponse;
import com.jendo.app.domain.notification.dto.NotificationReceiveDto;
import com.jendo.app.domain.notification.dto.NotificationResponseDto;
import com.jendo.app.domain.notification.entity.DeviceToken;
import com.jendo.app.domain.notification.repository.DeviceTokenRepository;
import com.jendo.app.domain.notification.service.NotificationService;
import com.jendo.app.domain.user.entity.User;
import com.jendo.app.domain.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Notification management endpoints")
public class NotificationController {

    private final DeviceTokenRepository tokenRepo;
    private final UserRepository userRepo;
    private final NotificationService notificationService;

    @PostMapping("/register-device")
    @Operation(summary = "Register device FCM token", description = "Registers a new device token for push notifications")
    public ResponseEntity<ApiResponse<Void>> registerDevice(
            @Valid @RequestBody RegisterDeviceRequest body) {
        try {
            User user = userRepo.findById(body.userId())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            tokenRepo.findByFcmToken(body.fcmToken()).ifPresentOrElse(
                    existing -> {
                        existing.setIsActive(true);
                        existing.setUser(user);
                        tokenRepo.save(existing);
                    },
                    () -> {
                        DeviceToken token = DeviceToken.builder()
                                .user(user)
                                .fcmToken(body.fcmToken())
                                .platform(body.platform())
                                .deviceModel(body.deviceInfo() != null ? body.deviceInfo().model : null)
                                .osVersion(body.deviceInfo() != null ? body.deviceInfo().osVersion : null)
                                .isActive(true)
                                .build();
                        tokenRepo.save(token);
                    }
            );

            return ResponseEntity.ok(ApiResponse.success(null, "Device registered successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to register device: " + e.getMessage()));
        }
    }

    @PostMapping("/receive")
    @Operation(summary = "Receive Firebase notification", description = "Saves incoming Firebase notification to database")
    public ResponseEntity<ApiResponse<NotificationResponseDto>> receiveNotification(
            @Valid @RequestBody NotificationReceiveDto request) {
        try {
            NotificationResponseDto notification = notificationService.saveReceivedNotification(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(notification, "Notification saved successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to save notification: " + e.getMessage()));
        }
    }

    // ✅ GET notifications by user ID with pagination
    @GetMapping("/user/{userId}")
    @Operation(summary = "Get notifications by user ID", description = "Fetch paginated notifications for a specific user")
    public ResponseEntity<ApiResponse<PaginationResponse<NotificationResponseDto>>> getNotificationsByUserId(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            PaginationResponse<NotificationResponseDto> response =
                    notificationService.getNotificationsByUserId(userId, page, size);
            return ResponseEntity.ok(ApiResponse.success(response, "Notifications fetched successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch notifications: " + e.getMessage()));
        }
    }

    // ✅ GET unread count for user
    @GetMapping("/user/{userId}/unread/count")
    @Operation(summary = "Get unread notification count", description = "Get the count of unread notifications for a user")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(@PathVariable Long userId) {
        try {
            long count = notificationService.getUnreadCountByUserId(userId);
            return ResponseEntity.ok(ApiResponse.success(count, "Unread count fetched successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch unread count: " + e.getMessage()));
        }
    }

    // ✅ PATCH mark notification as read
    @PatchMapping("/{notificationId}/read")
    @Operation(summary = "Mark notification as read", description = "Mark a specific notification as read")
    public ResponseEntity<ApiResponse<NotificationResponseDto>> markAsRead(@PathVariable Long notificationId) {
        try {
            NotificationResponseDto notification = notificationService.markAsRead(notificationId);
            return ResponseEntity.ok(ApiResponse.success(notification, "Notification marked as read"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to mark as read: " + e.getMessage()));
        }
    }

    // ✅ PATCH mark all notifications as read for user
    @PatchMapping("/user/{userId}/read-all")
    @Operation(summary = "Mark all notifications as read", description = "Mark all notifications as read for a user")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(@PathVariable Long userId) {
        try {
            notificationService.markAllAsRead(userId);
            return ResponseEntity.ok(ApiResponse.success(null, "All notifications marked as read"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to mark all as read: " + e.getMessage()));
        }
    }

    // ✅ DELETE a notification
    @DeleteMapping("/{notificationId}")
    @Operation(summary = "Delete notification", description = "Delete a specific notification")
    public ResponseEntity<ApiResponse<Void>> deleteNotification(@PathVariable Long notificationId) {
        try {
            notificationService.deleteNotification(notificationId);
            return ResponseEntity.ok(ApiResponse.success(null, "Notification deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to delete notification: " + e.getMessage()));
        }
    }

    public record RegisterDeviceRequest(
            @NotNull Long userId,
            @NotBlank String fcmToken,
            String platform,
            DeviceInfo deviceInfo
    ) {}

    @Data
    public static class DeviceInfo {
        public String model;
        public String osVersion;
    }
}