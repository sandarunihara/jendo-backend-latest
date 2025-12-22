package com.jendo.app.domain.jendotest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Jendo Test response data")
public class JendoTestResponseDto {

    @Schema(description = "Test unique identifier", example = "1")
    private Long id;

    @Schema(description = "ID of the user who took the test", example = "1")
    private Long userId;

    @Schema(description = "Name of the user who took the test", example = "John Doe")
    private String userName;

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

    @Schema(description = "PDF file path", example = "uploads/report-attachments/test-123.pdf")
    private String pdfFilePath;

    @Schema(description = "Test creation timestamp", example = "2024-01-15T10:30:00")
    private LocalDateTime createdAt;
}
