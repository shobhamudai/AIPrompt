package com.example.controller;

import com.example.service.BedrockService;
import org.springframework.beans.factory.annotation.Autowired;
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

    @PostMapping
    public Map<String, String> getPromptResponse(@RequestBody Map<String, String> payload) {
        String prompt = payload.get("prompt");
        String response = bedrockService.getCompletion(prompt);
        return Map.of("response", response);
    }
}
