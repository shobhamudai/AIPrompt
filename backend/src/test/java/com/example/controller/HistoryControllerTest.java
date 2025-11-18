package com.example.controller;

import com.example.model.ChatMessage;
import com.example.service.ChatHistoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HistoryController.class)
class HistoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ChatHistoryService chatHistoryService;

    @Test
    @WithMockUser(username = "test-user")
    void getHistory_shouldReturnListOfMessages() throws Exception {
        // Arrange
        String userId = "test-user";
        ChatMessage message = new ChatMessage();
        message.setPrompt("Prompt");
        message.setResponse("Response");
        List<ChatMessage> history = List.of(message);

        when(chatHistoryService.getHistoryForUser(userId)).thenReturn(history);

        // Act & Assert
        mockMvc.perform(get("/api/history")
                        .with(jwt().jwt(builder -> builder.subject(userId))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].prompt").value("Prompt"));
    }

    @Test
    @WithMockUser(username = "test-user")
    void deleteHistoryItem_shouldReturnNoContent() throws Exception {
        // Arrange
        String userId = "test-user";
        Long createdAt = 123456789L;

        // Act & Assert
        mockMvc.perform(delete("/api/history/{createdAt}", createdAt)
                        .with(jwt().jwt(builder -> builder.subject(userId))))
                .andExpect(status().isNoContent());

        // Verify that the service method was called
        verify(chatHistoryService).deleteMessage(userId, createdAt);
    }
}
