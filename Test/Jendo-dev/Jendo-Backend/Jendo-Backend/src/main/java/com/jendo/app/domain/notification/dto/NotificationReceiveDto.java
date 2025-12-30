package com.jendo.app.domain.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Notification received from Firebase")
public class NotificationReceiveDto {

    @NotNull(message = "User ID is required")
    @Positive(message = "User ID must be positive")
    @Schema(description = "ID of the user", example = "1", required = true)
    private Long userId;

    @NotBlank(message = "Message is required")
    @Schema(description = "Notification message", example = "Your test results are ready", required = true)
    private String message;

    @Schema(description = "Notification type", example = "TEST_REMINDER")
    private String type;

    @Schema(description = "FCM message ID from Firebase", example = "0:1234567890123%123456789abc")
    private String fcmMessageId;

    @Schema(description = "Additional data from Firebase notification")
    private Map<String, String> data;
}