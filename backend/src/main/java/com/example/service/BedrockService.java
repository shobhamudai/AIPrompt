package com.example.service;

import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelRequest;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelResponse;
import org.json.JSONObject;
import software.amazon.awssdk.core.SdkBytes;

@Service
public class BedrockService {

    private final BedrockRuntimeClient bedrockClient;

    // Constructor for Spring to auto-wire
    public BedrockService() {
        this.bedrockClient = BedrockRuntimeClient.builder()
                .region(Region.US_EAST_1)
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }

    // Constructor for testing
    public BedrockService(BedrockRuntimeClient bedrockClient) {
        this.bedrockClient = bedrockClient;
    }

    public String getCompletion(String prompt) {
        try {
            String modelId = "anthropic.claude-3-sonnet-20240229-v1:0";
            
            JSONObject content = new JSONObject();
            content.put("type", "text");
            content.put("text", prompt);

            JSONObject message = new JSONObject();
            message.put("role", "user");
            message.put("content", new JSONObject[]{content});

            JSONObject payload = new JSONObject();
            payload.put("anthropic_version", "bedrock-2023-05-31");
            payload.put("max_tokens", 1024);
            payload.put("messages", new JSONObject[]{message});

            SdkBytes body = SdkBytes.fromUtf8String(payload.toString());

            InvokeModelRequest request = InvokeModelRequest.builder()
                    .modelId(modelId)
                    .body(body)
                    .build();

            InvokeModelResponse response = bedrockClient.invokeModel(request);

            JSONObject responseBody = new JSONObject(response.body().asUtf8String());
            return responseBody.getJSONArray("content").getJSONObject(0).getString("text");
        } catch (Exception e) {
            e.printStackTrace();
            return "Error: Could not get a response from Bedrock.";
        }
    }
}
