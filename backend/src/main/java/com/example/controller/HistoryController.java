package com.example.controller;

import com.example.model.ChatMessage;
import com.example.service.ChatHistoryService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/history")
public class HistoryController {

    private final ChatHistoryService chatHistoryService;

    public HistoryController(ChatHistoryService chatHistoryService) {
        this.chatHistoryService = chatHistoryService;
    }

    @GetMapping
    public List<ChatMessage> getHistory(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        return chatHistoryService.getHistoryForUser(userId);
    }
}
