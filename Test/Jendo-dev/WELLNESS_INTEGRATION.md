# Wellness Recommendations Integration

## Overview
Implemented wellness recommendations based on user's latest Jendo test risk level. The system now automatically fetches personalized recommendations based on the risk level (LOW, MODERATE, HIGH) from the most recent test.

## Backend Changes

### 1. JendoTestRepository.java
**Location**: `src/main/java/com/jendo/app/domain/jendotest/repository/JendoTestRepository.java`

**Changes**:
- Added method to find user's latest test:
```java
@Query("SELECT j FROM JendoTest j WHERE j.user.id = :userId ORDER BY j.testDate DESC, j.createdAt DESC")
Optional<JendoTest> findLatestByUserId(Long userId);
```

### 2. WellnessRecommendationService.java
**Location**: `src/main/java/com/jendo/app/domain/wellnessrecommendation/service/WellnessRecommendationService.java`

**Changes**:
- Added new method signature:
```java
List<WellnessRecommendationDto> getRecommendationsForUser(Long userId);
```

### 3. WellnessRecommendationServiceImpl.java
**Location**: `src/main/java/com/jendo/app/domain/wellnessrecommendation/service/WellnessRecommendationServiceImpl.java`

**Changes**:
- Added dependency injection for `JendoTestRepository`
- Implemented new method to get recommendations based on user's latest test:
```java
@Override
@Transactional(readOnly = true)
public List<WellnessRecommendationDto> getRecommendationsForUser(Long userId) {
    // Find user's latest Jendo test
    Optional<JendoTest> latestTest = jendoTestRepository.findLatestByUserId(userId);
    
    if (latestTest.isEmpty()) {
        return Collections.emptyList();
    }
    
    String riskLevel = latestTest.get().getRiskLevel();
    
    if (riskLevel == null || riskLevel.trim().isEmpty()) {
        return Collections.emptyList();
    }
    
    // Get recommendations based on risk level
    return getByRiskLevel(riskLevel);
}
```

### 4. WellnessRecommendationController.java
**Location**: `src/main/java/com/jendo/app/controller/WellnessRecommendationController.java`

**Changes**:
- Added new endpoint:
```java
@GetMapping("/user/{userId}")
@Operation(summary = "Get wellness recommendations for user", 
           description = "Retrieves active wellness recommendations based on user's latest Jendo test risk level")
public ResponseEntity<ApiResponse<List<WellnessRecommendationDto>>> getRecommendationsForUser(
        @PathVariable Long userId) {
    List<WellnessRecommendationDto> recommendations = service.getRecommendationsForUser(userId);
    return ResponseEntity.ok(ApiResponse.success(recommendations));
}
```

**New API Endpoint**: `GET /api/wellness-recommendations/user/{userId}`

## Frontend Changes

### 1. endpoints.ts
**Location**: `src/infrastructure/api/endpoints.ts`

**Changes**:
- Added new endpoint configuration:
```typescript
FOR_USER: (userId: number) => `/wellness-recommendations/user/${userId}`,
```

### 2. wellnessRecommendationApi.ts
**Location**: `src/features/wellness/services/wellnessRecommendationApi.ts`

**Changes**:
- Added new API method:
```typescript
getForUser: async (userId: number): Promise<WellnessRecommendation[]> => {
  try {
    const response = await httpClient.get<ApiResponse<WellnessRecommendation[]>>(
      API_ENDPOINTS.WELLNESS.FOR_USER(userId)
    );
    return response.data || [];
  } catch (error) {
    console.error('Error fetching user wellness recommendations:', error);
    return [];
  }
}
```

### 3. Updated Wellness Screens
All wellness recommendation screens now use the simplified API call that automatically fetches recommendations based on the user's latest test:

#### DietSuggestionsScreen.tsx
**Location**: `src/features/wellness/screens/DietSuggestionsScreen.tsx`

**Changes**:
- Removed direct JendoTest API calls
- Now uses `wellnessRecommendationApi.getForUser(user.id)`
- Automatically filters for diet category recommendations

#### ExerciseTipsScreen.tsx
**Location**: `src/features/wellness/screens/ExerciseTipsScreen.tsx`

**Changes**:
- Updated to use `wellnessRecommendationApi.getForUser(user.id)`
- Filters for exercise category recommendations

#### SleepTipsScreen.tsx
**Location**: `src/features/wellness/screens/SleepTipsScreen.tsx`

**Changes**:
- Updated to use `wellnessRecommendationApi.getForUser(user.id)`
- Filters for lifestyle category recommendations

#### StressManagementScreen.tsx
**Location**: `src/features/wellness/screens/StressManagementScreen.tsx`

**Changes**:
- Updated to use `wellnessRecommendationApi.getForUser(user.id)`
- Filters for monitoring category recommendations

## How It Works

1. **User takes a Jendo test**: Test results are stored with a risk level (LOW, MODERATE, HIGH)
2. **User navigates to wellness recommendations**: Any wellness screen (Diet, Exercise, Sleep, Stress)
3. **Backend automatically finds latest test**: The new endpoint queries for the user's most recent test by `testDate DESC, createdAt DESC`
4. **Recommendations are filtered**: Returns only active recommendations matching the risk level
5. **Frontend displays personalized recommendations**: Each screen filters by category (diet, exercise, lifestyle, monitoring)

## Database Structure

### wellness_recommendations Table
- **id**: Primary key
- **title**: Recommendation title
- **description**: Detailed description
- **category**: Type of recommendation (diet, exercise, lifestyle, monitoring)
- **risk_level**: Associated risk level (LOW, MODERATE, HIGH)
- **type**: Additional classification
- **priority**: Display order
- **is_active**: Whether recommendation is currently active
- **created_at**: Creation timestamp
- **updated_at**: Last update timestamp

### Relationship
The system uses a **logical relationship** based on risk level matching:
- `jendo_tests.risk_level` → matches → `wellness_recommendations.risk_level`
- No physical foreign key constraint (allows flexibility)
- Query joins on risk level at runtime

## Benefits

1. **Personalized Recommendations**: Users get recommendations based on their actual health data
2. **Automatic Updates**: As users take new tests, recommendations automatically update
3. **Simplified Frontend**: Single API call replaces multiple queries
4. **Better Performance**: Backend handles the logic, reducing client-side processing
5. **Scalable**: Easy to add new recommendation categories or risk levels

## Testing

### Backend Testing
Test the new endpoint:
```bash
GET http://localhost:8080/api/wellness-recommendations/user/{userId}
```

### Frontend Testing
1. Ensure user has at least one Jendo test with a risk level
2. Navigate to any wellness screen (Diet, Exercise, Sleep, Stress)
3. Verify recommendations match the user's latest test risk level
4. Verify correct category filtering (diet recommendations on diet screen, etc.)

## Future Enhancements

1. Add physical foreign key if direct relationship needed
2. Implement recommendation versioning
3. Add user-specific recommendation customization
4. Track which recommendations users have viewed/followed
5. Add recommendation effectiveness tracking
