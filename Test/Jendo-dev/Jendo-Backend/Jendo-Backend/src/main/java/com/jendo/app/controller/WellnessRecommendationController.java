package com.jendo.app.controller;

import com.jendo.app.common.dto.ApiResponse;
import com.jendo.app.common.dto.PaginationResponse;
import com.jendo.app.domain.wellnessrecommendation.dto.WellnessRecommendationDto;
import com.jendo.app.domain.wellnessrecommendation.dto.WellnessRecommendationRequestDto;
import com.jendo.app.domain.wellnessrecommendation.service.WellnessRecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wellness-recommendations")
@RequiredArgsConstructor
@Tag(name = "Wellness Recommendations", description = "Wellness recommendation management APIs")
public class WellnessRecommendationController {

    private final WellnessRecommendationService service;

    @PostMapping
    @Operation(summary = "Create wellness recommendation", description = "Creates a new wellness recommendation (Admin only)")
    public ResponseEntity<ApiResponse<WellnessRecommendationDto>> create(
            @Valid @RequestBody WellnessRecommendationRequestDto request) {
        WellnessRecommendationDto recommendation = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(recommendation, "Wellness recommendation created successfully"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get wellness recommendation by ID", description = "Retrieves a wellness recommendation by ID")
    public ResponseEntity<ApiResponse<WellnessRecommendationDto>> getById(@PathVariable Long id) {
        WellnessRecommendationDto recommendation = service.getById(id);
        return ResponseEntity.ok(ApiResponse.success(recommendation));
    }

    @GetMapping
    @Operation(summary = "Get all wellness recommendations", description = "Retrieves all wellness recommendations with pagination")
    public ResponseEntity<ApiResponse<PaginationResponse<WellnessRecommendationDto>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PaginationResponse<WellnessRecommendationDto> recommendations = service.getAll(page, size);
        return ResponseEntity.ok(ApiResponse.success(recommendations));
    }

    @GetMapping("/risk-level/{riskLevel}")
    @Operation(summary = "Get wellness recommendations by risk level", 
               description = "Retrieves active wellness recommendations for a specific risk level (LOW, MODERATE, HIGH)")
    public ResponseEntity<ApiResponse<List<WellnessRecommendationDto>>> getByRiskLevel(
            @PathVariable String riskLevel) {
        List<WellnessRecommendationDto> recommendations = service.getByRiskLevel(riskLevel);
        return ResponseEntity.ok(ApiResponse.success(recommendations));
    }
    
    @GetMapping("/user/{userId}")
    @Operation(summary = "Get wellness recommendations for user", 
               description = "Retrieves active wellness recommendations based on user's latest Jendo test risk level")
    public ResponseEntity<ApiResponse<List<WellnessRecommendationDto>>> getRecommendationsForUser(
            @PathVariable Long userId) {
        List<WellnessRecommendationDto> recommendations = service.getRecommendationsForUser(userId);
        return ResponseEntity.ok(ApiResponse.success(recommendations));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update wellness recommendation", description = "Updates an existing wellness recommendation (Admin only)")
    public ResponseEntity<ApiResponse<WellnessRecommendationDto>> update(
            @PathVariable Long id,
            @Valid @RequestBody WellnessRecommendationRequestDto request) {
        WellnessRecommendationDto recommendation = service.update(id, request);
        return ResponseEntity.ok(ApiResponse.success(recommendation, "Wellness recommendation updated successfully"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete wellness recommendation", description = "Deletes a wellness recommendation (Admin only)")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
