package com.jendo.app.domain.chatbot.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jendo.app.domain.chatbot.dto.ChatRequest;
import com.jendo.app.domain.chatbot.dto.ChatResponse;
import com.jendo.app.domain.chatbot.service.ChatbotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatbotServiceImpl implements ChatbotService {
    
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    
    // Free Hugging Face API endpoints
    private static final String HF_API_URL_PRIMARY = "https://api-inference.huggingface.co/models/microsoft/DialoGPT-medium";
    private static final String HF_API_URL_BACKUP = "https://api-inference.huggingface.co/models/microsoft/DialoGPT-small";
    
    // Rule-based response patterns
    private static final Map<Pattern, String> RESPONSE_PATTERNS = initializePatterns();

    @Override
    public ChatResponse sendMessage(ChatRequest request) {
        String userMessage = request.getMessage().trim();
        log.info("Processing chatbot message: {}", userMessage);
        
        try {
            // Tier 1: Smart Rule-Based Responses (Primary - Instant)
            String ruleBasedResponse = getRuleBasedResponse(userMessage);
            if (ruleBasedResponse != null) {
                log.info("✅ Rule-based response matched");
                return createSuccessResponse(ruleBasedResponse);
            }
            
            // Tier 2: Free Hugging Face API (Secondary - AI-powered)
            String aiResponse = getHuggingFaceResponse(userMessage);
            if (aiResponse != null && !aiResponse.isBlank()) {
                log.info("✅ Hugging Face AI response received");
                String enrichedResponse = enrichHealthResponse(aiResponse);
                return createSuccessResponse(enrichedResponse);
            }
            
            // Tier 3: Comprehensive Fallback (Tertiary - Always available)
            log.info("ℹ️ Using comprehensive fallback response");
            return createComprehensiveFallback(userMessage);
            
        } catch (Exception e) {
            log.error("Error processing chatbot message", e);
            return createComprehensiveFallback(userMessage);
        }
    }
    
    // ==================== TIER 1: RULE-BASED RESPONSES ====================
    
    private static Map<Pattern, String> initializePatterns() {
        Map<Pattern, String> patterns = new LinkedHashMap<>();
        
        // Greetings
        patterns.put(Pattern.compile("(?i)^(hi|hello|hey|good morning|good afternoon|good evening|greetings).*"),
                "👋 Hello! I'm Jendo Health Assistant. I can help you learn about:\n\n" +
                "• Jendo cardiovascular health technology\n" +
                "• The Jendo non-invasive health test\n" +
                "• Cardiovascular health and prevention\n" +
                "• How to schedule a test\n\n" +
                "What would you like to know?");
        
        // About Jendo
        patterns.put(Pattern.compile("(?i).*(what is jendo|tell me about jendo|about jendo|jendo company).*"),
                "🫀 **About Jendo**\n\n" +
                "Jendo is an AI-powered, non-invasive cardiovascular health technology designed for early detection of vascular dysfunction.\n\n" +
                "**Key Features:**\n" +
                "• Non-invasive and painless\n" +
                "• Uses Photoplethysmography (PPG) + Digital Thermal Monitoring (DTM)\n" +
                "• AI-driven analysis\n" +
                "• Early cardiovascular risk detection\n\n" +
                "Jendo supports early detection and preventive care but does not replace professional medical advice.");
        
        // How it works
        patterns.put(Pattern.compile("(?i).*(how does.*work|how it works|jendo technology|jendo test procedure|test process).*"),
                "📋 **Jendo Health Test Procedure**\n\n" +
                "**Duration:** Approximately 15 minutes\n\n" +
                "**Steps:**\n" +
                "1. You lie down comfortably in a supine position\n" +
                "2. PPG and DTM sensors extract vascular signals\n" +
                "3. Brief pressure cuff occlusion and release\n" +
                "4. Continued signal monitoring\n" +
                "5. Data uploaded to cloud for AI analysis\n\n" +
                "✅ **Completely non-invasive** - No needles, no radiation, painless!\n\n" +
                "Jendo supports early detection and preventive care but does not replace professional medical advice.");
        
        // Heart health
        patterns.put(Pattern.compile("(?i).*(heart health|cardiovascular|heart disease|blood pressure|vascular health).*"),
                "❤️ **Cardiovascular Health Information**\n\n" +
                "Cardiovascular disease (CVD) is a leading cause of mortality globally. Many conditions remain unnoticed until serious events occur.\n\n" +
                "**Jendo Measures:**\n" +
                "• Endothelial function (blood vessel lining health)\n" +
                "• Vascular reactivity\n" +
                "• Early cardiovascular risk indicators\n\n" +
                "**Vascular Health Score:**\n" +
                "Jendo generates a score for predictive cardiovascular risk assessment, enabling early risk identification and preventive monitoring.\n\n" +
                "⚠️ **Important:** This is NOT a medical diagnosis and must be interpreted by healthcare professionals.\n\n" +
                "Jendo supports early detection and preventive care but does not replace professional medical advice.");
        
        // Safety and comfort
        patterns.put(Pattern.compile("(?i).*(safe|safety|painful|pain|comfortable|radiation|invasive).*"),
                "✅ **Jendo Test Safety & Comfort**\n\n" +
                "The Jendo test is:\n" +
                "• **Completely non-invasive** - No needles\n" +
                "• **No radiation**\n" +
                "• **Painless and comfortable**\n" +
                "• **Suitable for routine screening**\n" +
                "• **Quick** - Only 15 minutes\n\n" +
                "It's designed for preventive health monitoring with your comfort and safety as top priorities!");
        
        // Patents
        patterns.put(Pattern.compile("(?i).*(patent|patented|innovation|technology|research).*"),
                "🔬 **Jendo Patented Technology**\n\n" +
                "Jendo's core technology is protected by patents in:\n" +
                "• 🇯🇵 Japan\n" +
                "• 🇱🇰 Sri Lanka\n" +
                "• 🇺🇸 USA\n\n" +
                "**Patent Coverage:**\n" +
                "• Non-invasive vascular assessment methods\n" +
                "• Advanced signal processing techniques\n" +
                "• AI-based cardiovascular risk analysis\n\n" +
                "Our technology represents cutting-edge innovation in preventive cardiovascular health.");
        
        // Pricing and scheduling
        patterns.put(Pattern.compile("(?i).*(price|cost|how much|booking|schedule|appointment|availability).*"),
                "📅 **Scheduling & Availability**\n\n" +
                "To schedule a Jendo Health Test or inquire about pricing:\n\n" +
                "📞 **Call us:** 0766210120\n" +
                "📧 **Email:** info@jendoinnovations.com\n" +
                "🌐 **Website:** https://www.jendo.health/\n\n" +
                "Our team will be happy to assist you with scheduling and provide detailed information about test availability and pricing.");
        
        // Contact information
        patterns.put(Pattern.compile("(?i).*(contact|address|location|phone|email|reach you).*"),
                "📞 **Contact Jendo**\n\n" +
                "**Jendo Incorporation (USA)**\n" +
                "📍 251, Little Falls Drive, Wilmington, New Castle County, Delaware\n" +
                "📧 info@jendoinnovations.com\n" +
                "📞 0766210120\n\n" +
                "**AI Health R&D Centre**\n" +
                "📍 Bay X, Trace Expert City\n" +
                "📧 info@jendoinnovations.com\n" +
                "📞 0766210120\n\n" +
                "🌐 **Website:** https://www.jendo.health/");
        
        return patterns;
    }
    
    private String getRuleBasedResponse(String message) {
        String normalizedMessage = message.toLowerCase().trim();
        
        for (Map.Entry<Pattern, String> entry : RESPONSE_PATTERNS.entrySet()) {
            if (entry.getKey().matcher(normalizedMessage).matches()) {
                return entry.getValue();
            }
        }
        
        return null;
    }
    
    // ==================== TIER 2: HUGGING FACE FREE API ====================
    
    private String getHuggingFaceResponse(String userMessage) {
        try {
            // Prepare health-focused prompt
            String enhancedPrompt = "You are Jendo Health Assistant helping with cardiovascular health questions. " +
                    "User asks: " + userMessage;
            
            // Try primary model first
            String response = callHuggingFaceAPI(HF_API_URL_PRIMARY, enhancedPrompt);
            if (response != null) {
                return response;
            }
            
            // Fallback to backup model
            log.info("Primary model failed, trying backup model");
            response = callHuggingFaceAPI(HF_API_URL_BACKUP, enhancedPrompt);
            return response;
            
        } catch (Exception e) {
            log.warn("Hugging Face API failed: {}", e.getMessage());
            return null;
        }
    }
    
    private String callHuggingFaceAPI(String apiUrl, String prompt) {
        try {
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("inputs", prompt);
            
            ObjectNode parameters = objectMapper.createObjectNode();
            parameters.put("max_length", 200);
            parameters.put("temperature", 0.7);
            parameters.put("top_p", 0.9);
            requestBody.set("parameters", parameters);
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(15))
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(requestBody)))
                    .build();
            
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                JsonNode responseJson = objectMapper.readTree(response.body());
                
                if (responseJson.isArray() && responseJson.size() > 0) {
                    String generatedText = responseJson.get(0).get("generated_text").asText();
                    return cleanHuggingFaceResponse(generatedText, prompt);
                }
            } else {
                log.warn("Hugging Face API returned status: {}", response.statusCode());
            }
            
        } catch (Exception e) {
            log.warn("Error calling Hugging Face API: {}", e.getMessage());
        }
        
        return null;
    }
    
    private String cleanHuggingFaceResponse(String generatedText, String originalPrompt) {
        // Remove the original prompt from response
        String cleaned = generatedText.replace(originalPrompt, "").trim();
        
        // Remove common artifacts
        cleaned = cleaned.replaceAll("^User asks?:.*?(?=\\n|$)", "");
        cleaned = cleaned.replaceAll("^You are.*?Assistant.*?(?=\\n|$)", "");
        cleaned = cleaned.trim();
        
        return cleaned.isBlank() ? null : cleaned;
    }
    
    private String enrichHealthResponse(String aiResponse) {
        // Add health disclaimer if not present
        if (!aiResponse.toLowerCase().contains("medical advice") && 
            !aiResponse.toLowerCase().contains("consult") &&
            aiResponse.length() > 50) {
            
            return aiResponse + "\n\n⚠️ **Important:** Jendo supports early detection and preventive care but does not replace professional medical advice. " +
                    "For specific health concerns, please consult with a healthcare professional.";
        }
        
        return aiResponse;
    }
    
    // ==================== TIER 3: COMPREHENSIVE FALLBACK ====================
    
    private ChatResponse createComprehensiveFallback(String userMessage) {
        String fallbackMessage = """
            Thank you for your question! While I don't have a specific answer right now, I'm here to help you learn about Jendo cardiovascular health technology.
            
            🫀 **Quick Facts About Jendo:**
            • Non-invasive cardiovascular health assessment
            • AI-powered early risk detection
            • 15-minute painless test
            • Patented technology (USA, Japan, Sri Lanka)
            
            📋 **Common Topics I Can Help With:**
            • How the Jendo test works
            • Cardiovascular health information
            • Test safety and comfort
            • Scheduling and availability
            • Our patented technology
            
            📞 **Need More Information?**
            
            **Contact Us:**
            • Phone: 0766210120
            • Email: info@jendoinnovations.com
            • Website: https://www.jendo.health/
            
            **Jendo Incorporation (USA)**
            📍 251, Little Falls Drive, Wilmington, New Castle County, Delaware
            
            **AI Health R&D Centre**
            📍 Bay X, Trace Expert City
            
            Feel free to ask me anything about Jendo technology or cardiovascular health!
            
            ⚠️ **Important:** Jendo supports early detection and preventive care but does not replace professional medical advice.
            """;
        
        return ChatResponse.builder()
                .id("assistant-" + UUID.randomUUID())
                .role("assistant")
                .content(fallbackMessage)
                .timestamp(Instant.now().toString())
                .build();
    }
    
    // ==================== RESPONSE BUILDERS ====================
    
    private ChatResponse createSuccessResponse(String content) {
        return ChatResponse.builder()
                .id("assistant-" + UUID.randomUUID())
                .role("assistant")
                .content(content)
                .timestamp(Instant.now().toString())
                .build();
    }
}
