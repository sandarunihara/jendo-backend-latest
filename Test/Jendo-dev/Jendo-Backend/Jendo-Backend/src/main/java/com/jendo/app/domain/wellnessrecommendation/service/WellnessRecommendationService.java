package com.jendo.app.domain.wellnessrecommendation.service;

import com.jendo.app.common.dto.PaginationResponse;
import com.jendo.app.domain.wellnessrecommendation.dto.WellnessRecommendationDto;
import com.jendo.app.domain.wellnessrecommendation.dto.WellnessRecommendationRequestDto;

import java.util.List;
import java.util.Map;

public interface WellnessRecommendationService {
    
    WellnessRecommendationDto create(WellnessRecommendationRequestDto request);
    
    WellnessRecommendationDto getById(Long id);
    
    PaginationResponse<WellnessRecommendationDto> getAll(int page, int size);
    
    List<WellnessRecommendationDto> getByRiskLevel(String riskLevel);
    
    List<WellnessRecommendationDto> getRecommendationsForUser(Long userId);

    Map<String, List<WellnessRecommendationDto>> getDailyAiTips(Long userId);

    void generateDailyTipsForAllUsers();

    WellnessRecommendationDto update(Long id, WellnessRecommendationRequestDto request);
    
    void delete(Long id);
}
