# Chatbot Testing Guide 🧪

## Quick Start Testing

### 1. Start the Backend
```bash
cd "Test/Jendo-dev/Jendo-Backend/Jendo-Backend"
mvn spring-boot:run
```

Wait for: `Started JendoBackendApplication in X seconds`

### 2. Test Endpoints

#### Test 1: Simple Greeting (Rule-Based)
```bash
curl -X POST http://localhost:8080/api/chatbot/message \
  -H "Content-Type: application/json" \
  -d "{\"message\": \"Hello\"}"
```

**Expected:** Instant greeting response

#### Test 2: About Jendo (Rule-Based)
```bash
curl -X POST http://localhost:8080/api/chatbot/message \
  -H "Content-Type: application/json" \
  -d "{\"message\": \"What is Jendo?\"}"
```

**Expected:** Detailed Jendo technology explanation

#### Test 3: Test Procedure (Rule-Based)
```bash
curl -X POST http://localhost:8080/api/chatbot/message \
  -H "Content-Type: application/json" \
  -d "{\"message\": \"How does the Jendo test work?\"}"
```

**Expected:** Step-by-step procedure information

#### Test 4: Contact Info (Rule-Based)
```bash
curl -X POST http://localhost:8080/api/chatbot/message \
  -H "Content-Type: application/json" \
  -d "{\"message\": \"How can I contact you?\"}"
```

**Expected:** Full contact details

#### Test 5: Health Check Endpoint
```bash
curl -X POST http://localhost:8080/api/chatbot/test
```

**Expected:** Quick greeting response

#### Test 6: Complex Question (AI-Powered)
```bash
curl -X POST http://localhost:8080/api/chatbot/message \
  -H "Content-Type: application/json" \
  -d "{\"message\": \"Can you explain endothelial function in simple terms?\"}"
```

**Expected:** AI-generated response (may take 2-5 seconds)

### 3. Using Postman

**Import this collection:**

```json
{
  "info": {
    "name": "Jendo Chatbot Tests",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "Greeting",
      "request": {
        "method": "POST",
        "header": [{"key": "Content-Type", "value": "application/json"}],
        "body": {
          "mode": "raw",
          "raw": "{\"message\": \"Hello\"}"
        },
        "url": {"raw": "http://localhost:8080/api/chatbot/message"}
      }
    },
    {
      "name": "About Jendo",
      "request": {
        "method": "POST",
        "header": [{"key": "Content-Type", "value": "application/json"}],
        "body": {
          "mode": "raw",
          "raw": "{\"message\": \"What is Jendo?\"}"
        },
        "url": {"raw": "http://localhost:8080/api/chatbot/message"}
      }
    },
    {
      "name": "Test Endpoint",
      "request": {
        "method": "POST",
        "url": {"raw": "http://localhost:8080/api/chatbot/test"}
      }
    }
  ]
}
```

## Verification Checklist

✅ **Rule-Based Responses (Tier 1)**
- [ ] Greeting works instantly
- [ ] About Jendo provides full details
- [ ] Test procedure is clear
- [ ] Contact info is complete
- [ ] Safety questions answered
- [ ] Patent info provided
- [ ] Scheduling info available

✅ **AI Responses (Tier 2)**
- [ ] Complex questions get AI responses
- [ ] Response includes health disclaimer
- [ ] Takes 2-5 seconds
- [ ] Quality is acceptable

✅ **Fallback (Tier 3)**
- [ ] Unknown questions get comprehensive fallback
- [ ] Contact information included
- [ ] Jendo facts provided

## Expected Logs

### Successful Rule-Based Match
```
INFO  c.j.a.d.c.s.i.ChatbotServiceImpl : Processing chatbot message: Hello
INFO  c.j.a.d.c.s.i.ChatbotServiceImpl : ✅ Rule-based response matched
```

### AI Response
```
INFO  c.j.a.d.c.s.i.ChatbotServiceImpl : Processing chatbot message: complex question
INFO  c.j.a.d.c.s.i.ChatbotServiceImpl : ✅ Hugging Face AI response received
```

### Fallback
```
INFO  c.j.a.d.c.s.i.ChatbotServiceImpl : Processing chatbot message: unknown
INFO  c.j.a.d.c.s.i.ChatbotServiceImpl : ℹ️ Using comprehensive fallback response
```

## Integration with Frontend

### React/TypeScript Example
```typescript
const sendMessage = async (message: string) => {
  try {
    const response = await fetch('http://localhost:8080/api/chatbot/message', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ message })
    });
    
    const data = await response.json();
    console.log('Bot:', data.content);
  } catch (error) {
    console.error('Chat error:', error);
  }
};

// Usage
sendMessage('Hello');
sendMessage('What is Jendo?');
```

### Expo/React Native Example
```typescript
import axios from 'axios';

const chatbotApi = {
  sendMessage: async (message: string) => {
    const response = await axios.post(
      'http://localhost:8080/api/chatbot/message',
      { message }
    );
    return response.data;
  }
};

// Usage
const response = await chatbotApi.sendMessage('Hello');
console.log(response.content);
```

## Performance Testing

### Response Time Test
```bash
# Install httpie for better testing
pip install httpie

# Test with timing
time http POST localhost:8080/api/chatbot/message message="Hello"
```

**Expected:**
- Rule-based: < 0.1 seconds
- AI-powered: 2-5 seconds
- Fallback: < 0.1 seconds

### Load Testing (Optional)
```bash
# Install apache bench
# Then run:
ab -n 100 -c 10 -p payload.json -T application/json \
  http://localhost:8080/api/chatbot/message
```

Where `payload.json` contains:
```json
{"message": "Hello"}
```

## Common Issues

### Issue: Connection Refused
**Solution:** Ensure backend is running on port 8080

### Issue: 404 Not Found
**Solution:** Check URL is exactly: `http://localhost:8080/api/chatbot/message`

### Issue: Slow First Response
**Solution:** Normal! First AI request warms up the model (5-10 seconds)

### Issue: Generic Responses
**Solution:** Add more specific patterns to rule-based system

## Success Criteria

Your chatbot is working correctly if:
- ✅ Greetings get instant, friendly responses
- ✅ Jendo questions provide accurate information
- ✅ All responses include appropriate disclaimers
- ✅ System never fails (always returns something)
- ✅ No authentication errors
- ✅ No cost concerns

## 🎉 You're Done!

Your free LLM chatbot is now fully operational and ready for production use!
