package com.attendai.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.rekognition.RekognitionClient;
import software.amazon.awssdk.services.s3.S3Client;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * AWS Lambda Handler for Face Recognition.
 *
 * Triggered by: API Gateway POST /lambda/attendance/mark
 *
 * Flow:
 * 1. Parse Base64 image from API Gateway event body
 * 2. Upload image to S3 (attendance/ folder)
 * 3. Search for face in Rekognition collection
 * 4. Write attendance record to DynamoDB
 * 5. Return matched studentId, confidence, and status
 *
 * Environment variables required:
 *   AWS_REGION, S3_BUCKET_NAME, REKOGNITION_COLLECTION_ID,
 *   REKOGNITION_CONFIDENCE_THRESHOLD, DYNAMODB_TABLE_NAME
 */
public class FaceRecognitionHandler
        implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private static final Logger log = LoggerFactory.getLogger(FaceRecognitionHandler.class);
    private static final Gson gson = new Gson();

    // Initialize AWS clients as static fields (reused across Lambda invocations — warm starts)
    private static final String REGION = System.getenv().getOrDefault("AWS_REGION", "us-east-1");

    private static final S3Client s3Client = S3Client.builder()
            .region(Region.of(REGION))
            .credentialsProvider(DefaultCredentialsProvider.create())
            .build();

    private static final RekognitionClient rekognitionClient = RekognitionClient.builder()
            .region(Region.of(REGION))
            .credentialsProvider(DefaultCredentialsProvider.create())
            .build();

    private static final DynamoDbClient dynamoDbClient = DynamoDbClient.builder()
            .region(Region.of(REGION))
            .credentialsProvider(DefaultCredentialsProvider.create())
            .build();

    private final S3Uploader s3Uploader = new S3Uploader(s3Client);
    private final RekognitionMatcher matcher = new RekognitionMatcher(rekognitionClient);
    private final DynamoDBWriter dbWriter = new DynamoDBWriter(dynamoDbClient);

    @Override
    public APIGatewayProxyResponseEvent handleRequest(
            APIGatewayProxyRequestEvent event, Context context) {

        log.info("Lambda invoked: requestId={}", context.getAwsRequestId());

        try {
            // 1. Parse request body
            String body = event.getBody();
            if (body == null || body.isBlank()) {
                return errorResponse(400, "Request body is empty");
            }

            JsonObject requestJson = gson.fromJson(body, JsonObject.class);
            if (!requestJson.has("imageBase64")) {
                return errorResponse(400, "Missing required field: imageBase64");
            }

            String imageBase64 = requestJson.get("imageBase64").getAsString();
            String timestamp = Instant.now().toString();

            // 2. Upload image to S3
            String s3Key = s3Uploader.uploadImage(imageBase64, "attendance",
                    "lambda-" + timestamp.replace(":", "-"));
            log.info("Image uploaded to S3: {}", s3Key);

            // 3. Search face in Rekognition
            Optional<String[]> matchOpt = matcher.searchFace(imageBase64);

            String studentId;
            double confidence;
            String status;

            if (matchOpt.isPresent()) {
                studentId = matchOpt.get()[0];
                confidence = Double.parseDouble(matchOpt.get()[1]);
                status = "PRESENT";
                log.info("Match found: studentId={}, confidence={}%", studentId, confidence);
            } else {
                studentId = "UNKNOWN";
                confidence = 0.0;
                status = "ABSENT";
                log.info("No matching face found");
            }

            // 4. Write to DynamoDB
            dbWriter.writeRecord(studentId, timestamp, status, confidence, s3Key);

            // 5. Build success response
            JsonObject responseBody = new JsonObject();
            responseBody.addProperty("studentId", studentId);
            responseBody.addProperty("confidence", confidence);
            responseBody.addProperty("status", status);
            responseBody.addProperty("timestamp", timestamp);
            responseBody.addProperty("imageKey", s3Key);
            responseBody.addProperty("message",
                    "PRESENT".equals(status) ? "Attendance marked successfully" : "Face not recognized");

            return successResponse(200, responseBody.toString());

        } catch (Exception e) {
            log.error("Lambda execution failed: {}", e.getMessage(), e);
            return errorResponse(500, "Internal error: " + e.getMessage());
        }
    }

    private APIGatewayProxyResponseEvent successResponse(int statusCode, String body) {
        return buildResponse(statusCode, body);
    }

    private APIGatewayProxyResponseEvent errorResponse(int statusCode, String message) {
        JsonObject err = new JsonObject();
        err.addProperty("success", false);
        err.addProperty("message", message);
        return buildResponse(statusCode, err.toString());
    }

    private APIGatewayProxyResponseEvent buildResponse(int statusCode, String body) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Access-Control-Allow-Origin", "*");
        headers.put("Access-Control-Allow-Headers", "Content-Type,Authorization");
        headers.put("Access-Control-Allow-Methods", "POST,OPTIONS");

        return new APIGatewayProxyResponseEvent()
                .withStatusCode(statusCode)
                .withHeaders(headers)
                .withBody(body);
    }
}
