package com.jendo.app.domain.wellnessrecommendation.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jendo.app.common.dto.PaginationResponse;
import com.jendo.app.common.exceptions.NotFoundException;
import com.jendo.app.domain.jendotest.entity.JendoTest;
import com.jendo.app.domain.jendotest.repository.JendoTestRepository;
import com.jendo.app.domain.wellnessrecommendation.dto.WellnessRecommendationDto;
import com.jendo.app.domain.wellnessrecommendation.dto.WellnessRecommendationRequestDto;
import com.jendo.app.domain.wellnessrecommendation.entity.DailyAiTip;
import com.jendo.app.domain.wellnessrecommendation.entity.WellnessRecommendation;
import com.jendo.app.domain.wellnessrecommendation.repository.DailyAiTipRepository;
import com.jendo.app.domain.wellnessrecommendation.repository.WellnessRecommendationRepository;
import com.jendo.app.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class WellnessRecommendationServiceImpl implements WellnessRecommendationService {

    private final WellnessRecommendationRepository repository;
    private final JendoTestRepository jendoTestRepository;
    private final DailyAiTipRepository dailyAiTipRepository;
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;

    @Value("${groq.api.key:}")
    private String groqApiKey;

    private static final String GROQ_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final String GROQ_MODEL = "llama-3.3-70b-versatile";

    @Override
    public WellnessRecommendationDto create(WellnessRecommendationRequestDto request) {
        WellnessRecommendation entity = WellnessRecommendation.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .category(request.getCategory())
                .riskLevel(request.getRiskLevel())
                .type(request.getType())
                .priority(request.getPriority() != null ? request.getPriority() : 0)
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();
        
        WellnessRecommendation saved = repository.save(entity);
        return toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public WellnessRecommendationDto getById(Long id) {
        WellnessRecommendation entity = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Wellness recommendation not found with id: " + id));
        return toDto(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<WellnessRecommendationDto> getAll(int page, int size) {
        Page<WellnessRecommendation> pageResult = repository.findAll(
                PageRequest.of(page, size, Sort.by("priority").ascending().and(Sort.by("id").descending()))
        );
        
        List<WellnessRecommendationDto> content = pageResult.getContent().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        
        return PaginationResponse.<WellnessRecommendationDto>builder()
                .content(content)
                .pageNumber(page)
                .pageSize(size)
                .totalElements(pageResult.getTotalElements())
                .totalPages(pageResult.getTotalPages())
                .first(pageResult.isFirst())
                .last(pageResult.isLast())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<WellnessRecommendationDto> getByRiskLevel(String riskLevel) {
        List<WellnessRecommendation> recommendations = 
                repository.findByRiskLevelIgnoreCaseAndIsActiveTrueOrderByPriorityAsc(riskLevel);
        return recommendations.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<WellnessRecommendationDto> getRecommendationsForUser(Long userId) {
        // Find user's latest Jendo test
        Optional<JendoTest> latestTest = jendoTestRepository.findFirstByUserIdOrderByTestDateDescCreatedAtDesc(userId);
        
        if (latestTest.isEmpty()) {
            // No test found, return empty list or default recommendations
            return Collections.emptyList();
        }
        
        String riskLevel = latestTest.get().getRiskLevel();
        
        if (riskLevel == null || riskLevel.trim().isEmpty()) {
            // No risk level, return empty list
            return Collections.emptyList();
        }
        
        // Get recommendations based on risk level
        return getByRiskLevel(riskLevel);
    }

    @Override
    public Map<String, List<WellnessRecommendationDto>> getDailyAiTips(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        Window window = computeWindow(now);

        Optional<DailyAiTip> cached = dailyAiTipRepository
                .findFirstByUserIdAndWindowStartLessThanEqualAndWindowEndGreaterThanEqual(userId, now, now);
        if (cached.isPresent()) {
            return parsePayload(cached.get().getPayloadJson());
        }

        JendoTest latestTest = jendoTestRepository
                .findFirstByUserIdOrderByTestDateDescCreatedAtDesc(userId)
                .orElseThrow(() -> new NotFoundException("No Jendo test found for user id: " + userId));

        Map<String, List<WellnessRecommendationDto>> generated = generateAiTips(latestTest, window.start());
        persistPayload(userId, window, generated);
        return generated;
    }

    @Override
    public void generateDailyTipsForAllUsers() {
        LocalDateTime now = LocalDateTime.now();
        Window window = computeWindow(now);
        
        int totalUsers = 0;
        int skippedExisting = 0;
        int skippedNoTest = 0;
        int generated = 0;
        int failed = 0;

        for (var user : userRepository.findAll()) {
            totalUsers++;
            Long userId = user.getId();
            
            boolean exists = dailyAiTipRepository
                    .findFirstByUserIdAndWindowStartLessThanEqualAndWindowEndGreaterThanEqual(userId, now, now)
                    .isPresent();
            if (exists) {
                log.debug("User {} already has tips for current window, skipping", userId);
                skippedExisting++;
                continue;
            }
            
            Optional<JendoTest> latestTest = jendoTestRepository
                    .findFirstByUserIdOrderByTestDateDescCreatedAtDesc(userId);
            if (latestTest.isEmpty()) {
                log.debug("User {} has no Jendo test, skipping", userId);
                skippedNoTest++;
                continue;
            }
            
            try {
                log.info("Generating AI tips for user {}", userId);
                Map<String, List<WellnessRecommendationDto>> tips = generateAiTips(latestTest.get(), window.start());
                persistPayload(userId, window, tips);
                generated++;
                log.info("Successfully generated tips for user {}", userId);
            } catch (Exception ex) {
                failed++;
                log.error("Failed to pre-generate AI tips for user {}", userId, ex);
            }
        }
        
        log.info("Daily tips generation summary - Total users: {}, Generated: {}, Skipped (existing): {}, Skipped (no test): {}, Failed: {}",
                totalUsers, generated, skippedExisting, skippedNoTest, failed);
    }

    @Override
    public WellnessRecommendationDto update(Long id, WellnessRecommendationRequestDto request) {
        WellnessRecommendation entity = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Wellness recommendation not found with id: " + id));
        
        entity.setTitle(request.getTitle());
        entity.setDescription(request.getDescription());
        entity.setCategory(request.getCategory());
        entity.setRiskLevel(request.getRiskLevel());
        entity.setType(request.getType());
        if (request.getPriority() != null) {
            entity.setPriority(request.getPriority());
        }
        if (request.getIsActive() != null) {
            entity.setIsActive(request.getIsActive());
        }
        
        WellnessRecommendation updated = repository.save(entity);
        return toDto(updated);
    }

    @Override
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new NotFoundException("Wellness recommendation not found with id: " + id);
        }
        repository.deleteById(id);
    }

    private Window computeWindow(LocalDateTime now) {
        LocalTime anchor = LocalTime.of(6, 0);
        LocalDate startDate = now.toLocalTime().isBefore(anchor) ? now.toLocalDate().minusDays(1) : now.toLocalDate();
        LocalDateTime start = LocalDateTime.of(startDate, anchor);
        LocalDateTime end = start.plusDays(1).minusSeconds(1);
        return new Window(start, end);
    }

    private Map<String, List<WellnessRecommendationDto>> generateAiTips(JendoTest test, LocalDateTime windowStart) {
        log.debug("generateAiTips called for test ID: {}, risk level: {}", test.getId(), test.getRiskLevel());
        
        if (!StringUtils.hasText(groqApiKey)) {
            log.warn("Groq API key is missing; falling back to static recommendations");
            return fallbackByRisk(test.getRiskLevel());
        }

        log.debug("Groq API key present, building prompt...");
        String prompt = buildPrompt(test, windowStart.toLocalDate().getDayOfYear());
        log.debug("Prompt built, calling Groq API...");

        try {
            String responseJson = callGroq(prompt);
            log.debug("Groq response received: {}", responseJson != null ? responseJson.substring(0, Math.min(100, responseJson.length())) : "null");
            
            if (!StringUtils.hasText(responseJson)) {
                log.warn("Empty response from Groq, using fallback");
                return fallbackByRisk(test.getRiskLevel());
            }
            
            Map<String, List<WellnessRecommendationDto>> result = mapJsonToDto(responseJson, test.getRiskLevel());
            log.info("Successfully generated {} categories of tips from Groq", result.size());
            return result;
        } catch (Exception ex) {
            log.error("Groq API invocation failed", ex);
            return fallbackByRisk(test.getRiskLevel());
        }
    }

    private String buildPrompt(JendoTest t, int daySeed) {
        return """
You are a cardiometabolic wellness coach. Create 3 concise tips per category with an emoji prefix.
Categories: diet, exercise, sleep, stress.
Personalize using:
- risk_level: %s
- score: %s
- heart_rate: %s bpm
- blood_pressure: %s
- spo2: %s%%
- vascular_risk: %s
Day seed: %d (change tips daily).
Return JSON only:
{
    \"diet\": [{\"title\":\"..\", \"description\":\"short..\", \"longDescription\":\"detailed..\"}],
    \"exercise\": [...3 tips...],
    \"sleep\": [...3 tips...],
    \"stress\": [...3 tips...]
}
Keep each short description 20-35 words, each longDescription 40-60 words, friendly, actionable, avoid duplication across categories, include emojis.
""".formatted(
                t.getRiskLevel(),
                t.getScore(),
                t.getHeartRate(),
                t.getBloodPressure(),
                t.getSpo2(),
                t.getVascularRisk(),
                daySeed
        );
    }

    private String callGroq(String prompt) {
        log.debug("Calling Groq API with URL: {}", GROQ_URL);

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(groqApiKey);

        Map<String, Object> body = new HashMap<>();
        body.put("model", GROQ_MODEL);
        body.put("messages", List.of(Map.of(
                "role", "user",
                "content", prompt
        )));
        body.put("temperature", 0.7);
        body.put("max_tokens", 800);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.exchange(
                    GROQ_URL,
                    HttpMethod.POST,
                    entity,
                    Map.class
            ).getBody();

            if (response == null) {
                log.warn("Groq response body is null");
                return null;
            }

            var root = objectMapper.valueToTree(response);
            var choicesNode = root.path("choices");
            if (!choicesNode.isArray() || choicesNode.isEmpty()) {
                log.warn("Groq choices array is empty");
                return null;
            }

            var messageNode = choicesNode.get(0).path("message");
            var contentNode = messageNode.path("content");
            if (contentNode.isMissingNode() || contentNode.isNull()) {
                log.warn("Groq message content is missing");
                return null;
            }

            String text = contentNode.asText();
            log.debug("Raw Groq text response: {}", text);
            String sanitized = sanitizeJson(text);
            log.debug("Sanitized JSON: {}", sanitized);
            return sanitized;
        } catch (Exception ex) {
            log.error("Error calling Groq API", ex);
            throw ex;
        }
    }

    private Map<String, List<WellnessRecommendationDto>> mapJsonToDto(String json, String riskLevel) {
        log.debug("Mapping JSON to DTO, risk level: {}", riskLevel);
        log.debug("JSON to parse: {}", json);
        
        try {
            Map<String, List<Map<String, String>>> parsed = objectMapper.readValue(
                    json,
                    new TypeReference<>() {}
            );
            
            log.debug("Successfully parsed JSON into map with {} categories", parsed.size());

            Map<String, List<WellnessRecommendationDto>> result = new HashMap<>();

            parsed.forEach((category, items) -> {
                log.debug("Processing category: {} with {} items", category, items.size());
                List<WellnessRecommendationDto> dtos = items.stream()
                        .limit(3)
                        .map(item -> WellnessRecommendationDto.builder()
                                .title(item.getOrDefault("title", "Tip"))
                                .description(item.getOrDefault("description", ""))
                        .longDescription(item.getOrDefault("longDescription", item.getOrDefault("description", "")))
                                .category(category)
                                .riskLevel(riskLevel)
                                .priority(0)
                                .isActive(true)
                                .build())
                        .collect(Collectors.toList());
                result.put(category, dtos);
                log.debug("Added {} tips for category: {}", dtos.size(), category);
            });
            
            log.info("Successfully mapped {} categories with total tips", result.size());
            return result;
        } catch (Exception ex) {
            log.error("Failed to parse Groq JSON: {}", json, ex);
            return fallbackByRisk(riskLevel);
        }
    }

    private Map<String, List<WellnessRecommendationDto>> fallbackByRisk(String riskLevel) {
        List<WellnessRecommendationDto> list = getByRiskLevel(riskLevel);
        return list.stream()
                .collect(Collectors.groupingBy(
                        r -> StringUtils.hasText(r.getCategory()) ? r.getCategory() : "general",
                        Collectors.collectingAndThen(Collectors.toList(), values -> values.stream().limit(3).toList())
                ));
    }

    private void persistPayload(Long userId, Window window, Map<String, List<WellnessRecommendationDto>> payload) {
        log.debug("Persisting payload for user: {}, window: {} to {}", userId, window.start(), window.end());
        log.debug("Payload contains {} categories", payload.size());
        
        try {
            String json = objectMapper.writeValueAsString(payload);
            log.debug("Serialized payload JSON length: {} chars", json.length());
            log.debug("Serialized JSON preview: {}", json.substring(0, Math.min(200, json.length())));
            
            DailyAiTip entity = DailyAiTip.builder()
                    .userId(userId)
                    .windowStart(window.start())
                    .windowEnd(window.end())
                    .payloadJson(json)
                    .build();
            
            DailyAiTip saved = dailyAiTipRepository.save(entity);
            log.info("Successfully persisted AI tips for user {} with ID: {}", userId, saved.getId());
        } catch (Exception ex) {
            log.error("Failed to persist AI tips payload for user {}", userId, ex);
        }
    }

    private Map<String, List<WellnessRecommendationDto>> parsePayload(String payloadJson) {
        try {
            Map<String, List<Map<String, String>>> parsed = objectMapper.readValue(
                    payloadJson,
                    new TypeReference<>() {}
            );
            Map<String, List<WellnessRecommendationDto>> result = new HashMap<>();
            parsed.forEach((category, items) -> result.put(category, items.stream()
                    .map(item -> WellnessRecommendationDto.builder()
                            .title(item.getOrDefault("title", "Tip"))
                            .description(item.getOrDefault("description", ""))
                        .longDescription(item.getOrDefault("longDescription", item.getOrDefault("description", "")))
                            .category(category)
                            .riskLevel(item.getOrDefault("riskLevel", ""))
                            .priority(0)
                            .isActive(true)
                            .build())
                    .collect(Collectors.toList())));
            return result;
        } catch (Exception ex) {
            log.error("Failed to parse cached AI tips payload", ex);
            return Collections.emptyMap();
        }
    }

    private String sanitizeJson(String text) {
        if (text == null) {
            return null;
        }
        return text.replaceAll("(?s)```json", "").replaceAll("(?s)```", "").trim();
    }

    private WellnessRecommendationDto toDto(WellnessRecommendation entity) {
        return WellnessRecommendationDto.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .description(entity.getDescription())
            .longDescription(entity.getDescription())
                .category(entity.getCategory())
                .riskLevel(entity.getRiskLevel())
                .type(entity.getType())
                .priority(entity.getPriority())
                .isActive(entity.getIsActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private record Window(LocalDateTime start, LocalDateTime end) { }
}
