package com.jendo.app.domain.wellnessrecommendation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WellnessRecommendationDto {
    private Long id;
    private String title;
    private String description;
    private String longDescription;
    private String category;
    private String riskLevel;
    private String type;
    private Integer priority;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
