package com.example.dao;

import com.example.model.ChatMessage;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;

import java.util.List;

@Repository
public class ChatHistoryDao {

    private final DynamoDbTable<ChatMessage> chatHistoryTable;

    public ChatHistoryDao(DynamoDbEnhancedClient enhancedClient) {
        String tableName = System.getenv("CHAT_HISTORY_TABLE_NAME");
        this.chatHistoryTable = enhancedClient.table(tableName, TableSchema.fromBean(ChatMessage.class));
    }

    public void saveMessage(ChatMessage message) {
        chatHistoryTable.putItem(message);
    }

    public List<ChatMessage> getHistoryForUser(String userId) {
        Key key = Key.builder().partitionValue(userId).build();
        QueryEnhancedRequest request = QueryEnhancedRequest.builder()
                .queryConditional(QueryConditional.keyEqualTo(key))
                .build();

        return chatHistoryTable.query(request).items().stream().toList();
    }
}
