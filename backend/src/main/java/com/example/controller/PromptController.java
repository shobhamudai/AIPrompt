package com.example.controller;

import com.example.service.BedrockService;
import com.example.service.ChatHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/prompt")
public class PromptController {

    @Autowired
    private BedrockService bedrockService;

    @Autowired
    private ChatHistoryService chatHistoryService;

    @PostMapping
    public Map<String, String> getPromptResponse(@RequestBody Map<String, String> payload, @AuthenticationPrincipal Jwt jwt) {
        String prompt = payload.get("prompt");
        String response = bedrockService.getCompletion(prompt);
        String userId = jwt.getSubject();

        // Save the conversation to history
        chatHistoryService.saveMessage(userId, prompt, response);

        return Map.of("response", response);
    }
}
