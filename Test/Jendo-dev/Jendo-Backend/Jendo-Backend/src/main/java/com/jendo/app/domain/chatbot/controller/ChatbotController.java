package com.jendo.app.domain.chatbot.controller;

import com.jendo.app.domain.chatbot.dto.ChatRequest;
import com.jendo.app.domain.chatbot.dto.ChatResponse;
import com.jendo.app.domain.chatbot.service.ChatbotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chatbot")
@RequiredArgsConstructor
@Tag(name = "Chatbot", description = "Jendo Health Assistant chatbot endpoints")
public class ChatbotController {
    
    private final ChatbotService chatbotService;
    
    @PostMapping("/message")
    @Operation(summary = "Send a message to the Jendo Health Assistant - Free LLM with Smart Rules")
    public ResponseEntity<ChatResponse> sendMessage(@Valid @RequestBody ChatRequest request) {
        ChatResponse response = chatbotService.sendMessage(request);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/test")
    @Operation(summary = "Test chatbot endpoint - Quick health check")
    public ResponseEntity<ChatResponse> testChatbot() {
        ChatRequest testRequest = new ChatRequest();
        testRequest.setMessage("Hello");
        ChatResponse response = chatbotService.sendMessage(testRequest);
        return ResponseEntity.ok(response);
    }
}
