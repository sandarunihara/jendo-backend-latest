package com.jendo.app.domain.wellnessrecommendation.service;

import com.jendo.app.domain.wellnessrecommendation.repository.DailyAiTipRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class DailyAiTipCleanupJob {

    private final DailyAiTipRepository dailyAiTipRepository;

    @Scheduled(cron = "0 5 6 * * *")
    public void purgeExpired() {
        LocalDateTime now = LocalDateTime.now();
        try {
            dailyAiTipRepository.deleteExpired(now);
        } catch (Exception ex) {
            log.error("Failed to purge expired AI tips", ex);
            return;
        }
        log.info("Daily AI tips cleanup executed at {}", now);
    }
}
