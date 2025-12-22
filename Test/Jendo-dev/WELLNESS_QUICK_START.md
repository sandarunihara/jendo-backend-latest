# Quick Reference: Wellness Recommendations by Risk Level

## How to Test

### 1. Backend API
```bash
# Get recommendations for a specific user based on their latest test
GET http://localhost:8080/api/wellness-recommendations/user/{userId}

# Example response:
{
  "data": [
    {
      "id": 1,
      "title": "Reduce Salt Intake",
      "description": "Limit sodium to less than 2,300mg per day",
      "category": "diet",
      "riskLevel": "MODERATE",
      "type": "dietary",
      "priority": 1,
      "isActive": true
    },
    {
      "id": 2,
      "title": "Cardio Exercise",
      "description": "30 minutes of moderate exercise daily",
      "category": "exercise",
      "riskLevel": "MODERATE",
      "type": "physical",
      "priority": 2,
      "isActive": true
    }
  ],
  "message": "Success",
  "timestamp": "2024-01-20T10:00:00Z"
}
```

### 2. Create Test Data

#### Sample Jendo Test
```sql
INSERT INTO jendo_tests (user_id, score, heart_rate, risk_level, blood_pressure, spo2, vascular_risk, test_date, created_at)
VALUES (1, 75.5, 80, 'MODERATE', '120/80', 98.5, 25.5, '2024-01-20', NOW());
```

#### Sample Wellness Recommendations
```sql
-- Diet recommendations for MODERATE risk
INSERT INTO wellness_recommendations (title, description, category, risk_level, type, priority, is_active, created_at)
VALUES 
('Reduce Salt Intake', 'Limit sodium to less than 2,300mg per day', 'diet', 'MODERATE', 'dietary', 1, true, NOW()),
('Increase Fiber', 'Aim for 25-30g of fiber daily', 'diet', 'MODERATE', 'dietary', 2, true, NOW()),
('Healthy Fats', 'Include omega-3 rich foods like fish and nuts', 'diet', 'MODERATE', 'dietary', 3, true, NOW());

-- Exercise recommendations for MODERATE risk
INSERT INTO wellness_recommendations (title, description, category, risk_level, type, priority, is_active, created_at)
VALUES 
('Cardio Exercise', '30 minutes of moderate exercise daily', 'exercise', 'MODERATE', 'physical', 1, true, NOW()),
('Strength Training', 'Include resistance training 2-3 times per week', 'exercise', 'MODERATE', 'physical', 2, true, NOW());

-- Lifestyle recommendations for MODERATE risk
INSERT INTO wellness_recommendations (title, description, category, risk_level, type, priority, is_active, created_at)
VALUES 
('Sleep Schedule', 'Maintain 7-8 hours of quality sleep', 'lifestyle', 'MODERATE', 'sleep', 1, true, NOW()),
('Stress Management', 'Practice relaxation techniques daily', 'lifestyle', 'MODERATE', 'mental', 2, true, NOW());

-- Monitoring recommendations for MODERATE risk
INSERT INTO wellness_recommendations (title, description, category, risk_level, type, priority, is_active, created_at)
VALUES 
('Blood Pressure Monitoring', 'Check blood pressure weekly', 'monitoring', 'MODERATE', 'health', 1, true, NOW()),
('Regular Check-ups', 'Visit your doctor quarterly', 'monitoring', 'MODERATE', 'health', 2, true, NOW());
```

## Risk Levels

### LOW Risk
- User has good cardiovascular health
- General wellness recommendations
- Preventive measures

### MODERATE Risk
- User shows some risk factors
- More specific dietary and exercise recommendations
- Regular monitoring advised

### HIGH Risk
- User has significant risk factors
- Urgent lifestyle changes needed
- Frequent monitoring required
- Medical consultation recommended

## Categories

| Category | Description | Screen |
|----------|-------------|--------|
| `diet` | Dietary recommendations | Diet Suggestions |
| `exercise` | Physical activity recommendations | Exercise Tips |
| `lifestyle` | Sleep and lifestyle recommendations | Sleep Tips |
| `monitoring` | Health monitoring recommendations | Stress Management |

## Frontend Implementation

### API Call
```typescript
// In any wellness screen
const recommendations = await wellnessRecommendationApi.getForUser(user.id);

// Filter by category
const dietRecs = recommendations.filter(r => r.category?.toLowerCase() === 'diet');
const exerciseRecs = recommendations.filter(r => r.category?.toLowerCase() === 'exercise');
const lifestyleRecs = recommendations.filter(r => r.category?.toLowerCase() === 'lifestyle');
const monitoringRecs = recommendations.filter(r => r.category?.toLowerCase() === 'monitoring');
```

### Risk Level Display
```typescript
const riskLevel = recommendations[0]?.riskLevel?.toLowerCase() || 'low';

// Get appropriate colors
const color = getRiskColor(riskLevel); // '#4CAF50', '#FF9800', or '#F44336'
const bgColor = getRiskBgColor(riskLevel); // Light versions
```

## Database Query Logic

The system automatically:
1. Finds user's latest test: `ORDER BY test_date DESC, created_at DESC LIMIT 1`
2. Extracts risk level from that test
3. Returns all active recommendations matching that risk level
4. Frontend filters by category

## Troubleshooting

### No recommendations showing?
1. Check if user has any Jendo tests: `SELECT * FROM jendo_tests WHERE user_id = ?`
2. Check if test has risk_level: Should be 'LOW', 'MODERATE', or 'HIGH'
3. Check if recommendations exist: `SELECT * FROM wellness_recommendations WHERE risk_level = ? AND is_active = true`

### Wrong risk level showing?
1. Verify latest test: `SELECT * FROM jendo_tests WHERE user_id = ? ORDER BY test_date DESC, created_at DESC LIMIT 1`
2. Check test's risk_level value
3. Clear frontend cache and reload

### Recommendations not filtered by category?
1. Check wellness_recommendations.category values in database
2. Ensure frontend filter matches: `category?.toLowerCase() === 'diet'`
3. Verify category values are lowercase in comparison

## Next Steps

1. **Test the API**
   - Use Postman/Insomnia to call `/api/wellness-recommendations/user/{userId}`
   - Verify correct recommendations are returned

2. **Populate Database**
   - Add sample wellness recommendations for each risk level
   - Add sample Jendo tests with different risk levels

3. **Test Frontend**
   - Run the app and navigate to Diet Suggestions
   - Verify recommendations appear
   - Check if risk level badge shows correct color

4. **Verify All Categories**
   - Test Diet Suggestions (category: 'diet')
   - Test Exercise Tips (category: 'exercise')
   - Test Sleep Tips (category: 'lifestyle')
   - Test Stress Management (category: 'monitoring')
