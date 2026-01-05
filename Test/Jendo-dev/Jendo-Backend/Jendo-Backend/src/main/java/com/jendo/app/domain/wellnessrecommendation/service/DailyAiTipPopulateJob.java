package com.jendo.app.domain.wellnessrecommendation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DailyAiTipPopulateJob {

    private final WellnessRecommendationService wellnessRecommendationService;

    @Scheduled(cron = "0 0 6 * * *")
    public void populateDailyTipsForAllUsers() {
        try {
            wellnessRecommendationService.generateDailyTipsForAllUsers();
            log.info("Daily AI tips pre-generation completed at 06:00 for all users");
        } catch (Exception ex) {
            log.error("Daily AI tips pre-generation failed", ex);
        }
    }
}
