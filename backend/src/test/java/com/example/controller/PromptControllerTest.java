package com.example.controller;

import com.example.service.BedrockService;
import com.example.service.ChatHistoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PromptController.class)
class PromptControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BedrockService bedrockService;

    @MockBean
    private ChatHistoryService chatHistoryService;

    @Test
    @WithMockUser(username = "test-user")
    void getPromptResponse_shouldReturnResponseAndSaveHistory() throws Exception {
        // Arrange
        String prompt = "What is Java?";
        String response = "Java is a programming language.";
        String requestBody = "{\"prompt\":\"" + prompt + "\"}";
        String userId = "test-user";

        when(bedrockService.getCompletion(prompt)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/prompt")
                        .with(jwt().jwt(builder -> builder.subject(userId)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").value(response));

        // Verify that the history service was called
        verify(chatHistoryService).saveMessage(eq(userId), eq(prompt), eq(response));
    }
}
