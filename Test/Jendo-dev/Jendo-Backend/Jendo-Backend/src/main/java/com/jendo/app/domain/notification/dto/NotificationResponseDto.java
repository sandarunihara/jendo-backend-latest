package com.jendo.app.domain.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Notification response DTO")
public class NotificationResponseDto {

    @Schema(description = "Notification ID", example = "1")
    private Long id;

    @Schema(description = "User ID", example = "1")
    private Long userId;

    @Schema(description = "Notification message", example = "You have an appointment tomorrow")
    private String message;

    @Schema(description = "Notification type", example = "APPOINTMENT_REMINDER")
    private String type;

    @Schema(description = "Whether notification has been read", example = "false")
    private Boolean isRead;

    @Schema(description = "When notification was created", example = "2025-12-30T18:00:00")
    private LocalDateTime createdAt;
}