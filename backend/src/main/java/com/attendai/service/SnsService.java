package com.attendai.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;

/**
 * Service for AWS SNS notifications.
 * Sends email/SMS alerts when attendance is marked.
 */
@Service
public class SnsService {

    private static final Logger log = LoggerFactory.getLogger(SnsService.class);

    @Autowired
    private SnsClient snsClient;

    @Value("${aws.sns.topic-arn:}")
    private String topicArn;

    /**
     * Publish an attendance notification to the SNS topic.
     * Subscribers (email/SMS) will receive the message.
     *
     * @param studentId   matched student ID
     * @param studentName student's display name
     * @param status      PRESENT or ABSENT
     * @param confidence  Rekognition confidence score
     * @param timestamp   ISO-8601 timestamp string
     */
    public void publishAttendanceNotification(String studentId, String studentName,
                                              String status, double confidence, String timestamp) {
        if (topicArn == null || topicArn.isBlank()) {
            log.warn("SNS Topic ARN not configured — skipping notification");
            return;
        }

        String subject = String.format("AttendAI: %s marked %s", studentName, status);
        String message = String.format(
                "Attendance Update\n" +
                "=================\n" +
                "Student:    %s (%s)\n" +
                "Status:     %s\n" +
                "Confidence: %.2f%%\n" +
                "Time:       %s\n\n" +
                "— AttendAI Cloud System",
                studentName, studentId, status, confidence, timestamp
        );

        PublishRequest request = PublishRequest.builder()
                .topicArn(topicArn)
                .subject(subject)
                .message(message)
                .build();

        PublishResponse response = snsClient.publish(request);
        log.info("SNS notification sent. MessageId: {}", response.messageId());
    }

    /**
     * Publish a custom message to the SNS topic.
     */
    public void publishMessage(String subject, String message) {
        if (topicArn == null || topicArn.isBlank()) {
            log.warn("SNS Topic ARN not configured — skipping notification");
            return;
        }
        PublishRequest request = PublishRequest.builder()
                .topicArn(topicArn)
                .subject(subject)
                .message(message)
                .build();
        PublishResponse response = snsClient.publish(request);
        log.info("SNS message published. MessageId: {}", response.messageId());
    }
}
