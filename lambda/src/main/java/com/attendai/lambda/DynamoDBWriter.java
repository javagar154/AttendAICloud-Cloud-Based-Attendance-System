package com.attendai.lambda;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * Helper class for writing attendance records to DynamoDB within the Lambda function.
 */
public class DynamoDBWriter {

    private static final Logger log = LoggerFactory.getLogger(DynamoDBWriter.class);
    private static final String TABLE_NAME =
            System.getenv().getOrDefault("DYNAMODB_TABLE_NAME", "AttendanceRecords");

    private final DynamoDbClient dynamoDbClient;

    public DynamoDBWriter(DynamoDbClient dynamoDbClient) {
        this.dynamoDbClient = dynamoDbClient;
    }

    /**
     * Write an attendance record to DynamoDB.
     *
     * @param studentId  matched student ID (or "UNKNOWN")
     * @param timestamp  ISO-8601 timestamp
     * @param status     "PRESENT" or "ABSENT"
     * @param confidence Rekognition confidence score
     * @param imageKey   S3 key of the captured image
     */
    public void writeRecord(String studentId, String timestamp, String status,
                            double confidence, String imageKey) {
        Map<String, AttributeValue> item = new HashMap<>();
        item.put("studentId", AttributeValue.builder().s(studentId).build());
        item.put("timestamp", AttributeValue.builder().s(timestamp).build());
        item.put("status", AttributeValue.builder().s(status).build());
        item.put("confidence", AttributeValue.builder().n(String.valueOf(confidence)).build());
        item.put("imageKey", AttributeValue.builder().s(imageKey != null ? imageKey : "").build());
        // Store date separately for easy querying by date
        item.put("date", AttributeValue.builder().s(timestamp.substring(0, 10)).build());
        // Mark this record as written by Lambda (audit trail)
        item.put("source", AttributeValue.builder().s("lambda").build());

        PutItemRequest request = PutItemRequest.builder()
                .tableName(TABLE_NAME)
                .item(item)
                .build();

        dynamoDbClient.putItem(request);
        log.info("DynamoDB record written: studentId={}, status={}", studentId, status);
    }
}
