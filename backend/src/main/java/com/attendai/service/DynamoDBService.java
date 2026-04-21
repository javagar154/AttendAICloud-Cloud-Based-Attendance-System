package com.attendai.service;

import com.attendai.model.AttendanceRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for DynamoDB operations.
 * Table schema:
 *   PK: studentId (String)
 *   SK: timestamp (String — ISO-8601)
 *   Fields: status, confidence, studentName, imageKey
 */
@Service
public class DynamoDBService {

    private static final Logger log = LoggerFactory.getLogger(DynamoDBService.class);

    @Autowired
    private DynamoDbClient dynamoDbClient;

    @Value("${aws.dynamodb.table-name}")
    private String tableName;

    /**
     * Save an attendance record to DynamoDB.
     */
    public void saveAttendanceRecord(AttendanceRecord record) {
        if ("fake_access_key".equals(System.getenv("AWS_ACCESS_KEY_ID"))) {
            log.info("[DUMMY MODE] Mocking DynamoDB saveAttendanceRecord");
            return;
        }
        Map<String, AttributeValue> item = new HashMap<>();
        item.put("studentId", AttributeValue.builder().s(record.getStudentId()).build());
        item.put("timestamp", AttributeValue.builder().s(record.getTimestamp()).build());
        item.put("status", AttributeValue.builder().s(record.getStatus()).build());
        item.put("studentName", AttributeValue.builder().s(
                record.getStudentName() != null ? record.getStudentName() : "").build());
        if (record.getConfidence() != null) {
            item.put("confidence", AttributeValue.builder().n(String.valueOf(record.getConfidence())).build());
        }
        if (record.getImageKey() != null) {
            item.put("imageKey", AttributeValue.builder().s(record.getImageKey()).build());
        }
        // Add date for easy filtering
        item.put("date", AttributeValue.builder().s(
                record.getTimestamp().substring(0, 10)).build());

        PutItemRequest request = PutItemRequest.builder()
                .tableName(tableName)
                .item(item)
                .build();

        dynamoDbClient.putItem(request);
        log.info("Saved attendance record: studentId={}, status={}", record.getStudentId(), record.getStatus());
    }

    /**
     * Get all attendance records (optionally filtered by studentId).
     */
    public List<AttendanceRecord> getAttendanceRecords(String studentId) {
        List<AttendanceRecord> results = new ArrayList<>();

        if ("fake_access_key".equals(System.getenv("AWS_ACCESS_KEY_ID"))) {
            log.info("[DUMMY MODE] Mocking DynamoDB getAttendanceRecords");
            AttendanceRecord mockRecord = new AttendanceRecord();
            mockRecord.setStudentId(studentId != null && !studentId.isBlank() ? studentId : "23105019");
            mockRecord.setStudentName("Mock Student");
            mockRecord.setTimestamp(Instant.now().toString());
            mockRecord.setStatus("PRESENT");
            mockRecord.setConfidence(99.9);
            results.add(mockRecord);
            return results;
        }

        if (studentId != null && !studentId.isBlank()) {
            // Query by PK
            QueryRequest request = QueryRequest.builder()
                    .tableName(tableName)
                    .keyConditionExpression("studentId = :sid")
                    .expressionAttributeValues(Map.of(
                            ":sid", AttributeValue.builder().s(studentId).build()
                    ))
                    .build();

            QueryResponse response = dynamoDbClient.query(request);
            response.items().forEach(item -> results.add(mapToRecord(item)));
        } else {
            // Scan all records
            ScanResponse response = dynamoDbClient.scan(ScanRequest.builder().tableName(tableName).build());
            response.items().forEach(item -> results.add(mapToRecord(item)));
        }

        return results;
    }

    /**
     * Get today's attendance summary.
     */
    public Map<String, Object> getTodaySummary() {
        if ("fake_access_key".equals(System.getenv("AWS_ACCESS_KEY_ID"))) {
            log.info("[DUMMY MODE] Mocking DynamoDB getTodaySummary");
            Map<String, Object> summary = new HashMap<>();
            summary.put("totalStudents", 50L);
            summary.put("present", 45L);
            summary.put("absent", 5L);
            summary.put("percentage", 90.0);
            return summary;
        }

        String today = LocalDate.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_LOCAL_DATE);

        // Scan with filter for today's date
        ScanRequest request = ScanRequest.builder()
                .tableName(tableName)
                .filterExpression("#d = :today")
                .expressionAttributeNames(Map.of("#d", "date"))
                .expressionAttributeValues(Map.of(":today", AttributeValue.builder().s(today).build()))
                .build();

        ScanResponse response = dynamoDbClient.scan(request);
        long present = response.items().stream()
                .filter(i -> "PRESENT".equals(i.get("status") != null ? i.get("status").s() : ""))
                .count();
        long total = response.count();
        long absent = total - present;
        double percentage = total > 0 ? (present * 100.0 / total) : 0.0;

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalStudents", total);
        summary.put("present", present);
        summary.put("absent", absent);
        summary.put("percentage", Math.round(percentage * 10.0) / 10.0);
        return summary;
    }

    private AttendanceRecord mapToRecord(Map<String, AttributeValue> item) {
        AttendanceRecord record = new AttendanceRecord();
        record.setStudentId(getStr(item, "studentId"));
        record.setStudentName(getStr(item, "studentName"));
        record.setTimestamp(getStr(item, "timestamp"));
        record.setStatus(getStr(item, "status"));
        record.setImageKey(getStr(item, "imageKey"));
        if (item.containsKey("confidence") && item.get("confidence").n() != null) {
            record.setConfidence(Double.parseDouble(item.get("confidence").n()));
        }
        return record;
    }

    private String getStr(Map<String, AttributeValue> item, String key) {
        return item.containsKey(key) ? item.get(key).s() : null;
    }
}
