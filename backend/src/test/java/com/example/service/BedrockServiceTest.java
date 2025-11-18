package com.example.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelRequest;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BedrockServiceTest {

    @Mock
    private BedrockRuntimeClient bedrockClient;

    // Cannot use @InjectMocks because the real BedrockService constructor creates a real client.
    private BedrockService bedrockService;

    @BeforeEach
    void setUp() {
        // Manually create the service and inject the mock client
        bedrockService = new BedrockService(bedrockClient);
    }

    @Test
    void getCompletion_shouldReturnParsedText_whenApiSucceeds() {
        // Arrange
        String prompt = "Test prompt";
        String expectedResponse = "This is a test response.";
        String mockApiResponse = "{\"content\":[{\"type\":\"text\",\"text\":\"" + expectedResponse + "\"}]}";

        InvokeModelResponse mockResponse = InvokeModelResponse.builder()
                .body(SdkBytes.fromUtf8String(mockApiResponse))
                .build();

        when(bedrockClient.invokeModel(any(InvokeModelRequest.class))).thenReturn(mockResponse);

        // Act
        String actualResponse = bedrockService.getCompletion(prompt);

        // Assert
        assertEquals(expectedResponse, actualResponse);
    }

    @Test
    void getCompletion_shouldReturnErrorMessage_whenApiFails() {
        // Arrange
        String prompt = "Test prompt";
        when(bedrockClient.invokeModel(any(InvokeModelRequest.class))).thenThrow(new RuntimeException("API Error"));

        // Act
        String actualResponse = bedrockService.getCompletion(prompt);

        // Assert
        assertEquals("Error: Could not get a response from Bedrock.", actualResponse);
    }
}
