package com.jendo.app.domain.jendotest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Jendo Test creation/update request")
public class JendoTestRequestDto {

    @NotNull(message = "User ID is required")
    @Positive(message = "User ID must be positive")
    @Schema(description = "ID of the user taking the test", example = "1", required = true)
    private Long userId;

    @Schema(description = "Test score", example = "85.5")
    private BigDecimal score;

    @Schema(description = "Heart rate during test", example = "72")
    private Integer heartRate;

    @Schema(description = "Risk level assessment", example = "LOW")
    private String riskLevel;

    @Schema(description = "Time of test", example = "14:30:00")
    private LocalTime testTime;

    @Schema(description = "Blood pressure reading", example = "120/80")
    private String bloodPressure;

    @Schema(description = "SpO2 oxygen saturation", example = "98.5")
    private BigDecimal spo2;

    @Schema(description = "Date of test", example = "2024-01-15")
    private LocalDate testDate;

    @Schema(description = "Vascular risk percentage", example = "25.5")
    private BigDecimal vascularRisk;

    @Schema(description = "PDF file path (optional)", example = "uploads/report-attachments/test-123.pdf")
    private String pdfFilePath;
}
