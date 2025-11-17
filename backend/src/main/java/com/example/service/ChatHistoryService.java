package com.example.service;

import com.example.dao.ChatHistoryDao;
import com.example.model.ChatMessage;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatHistoryService {

    private final ChatHistoryDao chatHistoryDao;

    public ChatHistoryService(ChatHistoryDao chatHistoryDao) {
        this.chatHistoryDao = chatHistoryDao;
    }

    public void saveMessage(String userId, String prompt, String response) {
        ChatMessage message = new ChatMessage();
        message.setUserId(userId);
        message.setCreatedAt(System.currentTimeMillis());
        message.setPrompt(prompt);
        message.setResponse(response);
        chatHistoryDao.saveMessage(message);
    }

    public List<ChatMessage> getHistoryForUser(String userId) {
        return chatHistoryDao.getHistoryForUser(userId);
    }
}
