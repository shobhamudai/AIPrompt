package com.example.service;

import com.example.dao.ChatHistoryDao;
import com.example.model.ChatMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatHistoryServiceTest {

    @Mock
    private ChatHistoryDao chatHistoryDao;

    @InjectMocks
    private ChatHistoryService chatHistoryService;

    @Test
    void saveMessage_shouldCallDaoWithCorrectlyFormattedMessage() {
        // Arrange
        String userId = "test-user";
        String prompt = "Hello";
        String response = "Hi there!";
        ArgumentCaptor<ChatMessage> messageCaptor = ArgumentCaptor.forClass(ChatMessage.class);

        // Act
        chatHistoryService.saveMessage(userId, prompt, response);

        // Assert
        verify(chatHistoryDao).saveMessage(messageCaptor.capture());
        ChatMessage capturedMessage = messageCaptor.getValue();

        assertEquals(userId, capturedMessage.getUserId());
        assertEquals(prompt, capturedMessage.getPrompt());
        assertEquals(response, capturedMessage.getResponse());
        assertNotNull(capturedMessage.getCreatedAt());
    }

    @Test
    void getHistoryForUser_shouldReturnListOfMessages() {
        // Arrange
        String userId = "test-user";
        List<ChatMessage> expectedHistory = List.of(new ChatMessage(), new ChatMessage());
        when(chatHistoryDao.getHistoryForUser(userId)).thenReturn(expectedHistory);

        // Act
        List<ChatMessage> actualHistory = chatHistoryService.getHistoryForUser(userId);

        // Assert
        assertEquals(2, actualHistory.size());
        verify(chatHistoryDao).getHistoryForUser(userId);
    }

    @Test
    void deleteMessage_shouldCallDaoWithCorrectParameters() {
        // Arrange
        String userId = "test-user";
        Long createdAt = 12345L;

        // Act
        chatHistoryService.deleteMessage(userId, createdAt);

        // Assert
        verify(chatHistoryDao).deleteMessage(userId, createdAt);
    }
}
