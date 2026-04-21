package com.attendai.controller;

import com.attendai.model.ApiResponse;
import com.attendai.model.AttendanceRecord;
import com.attendai.service.DynamoDBService;
import com.attendai.service.RekognitionService;
import com.attendai.service.S3Service;
import com.attendai.service.SnsService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Attendance controller.
 * POST /api/attendance/mark   — Submit face image, match in Rekognition, record attendance
 * GET  /api/attendance/list   — Get attendance records (optional ?studentId= filter)
 * GET  /api/attendance/summary — Get today's attendance stats
 * All endpoints require a valid JWT.
 */
@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    private static final Logger log = LoggerFactory.getLogger(AttendanceController.class);

    @Autowired
    private RekognitionService rekognitionService;

    @Autowired
    private S3Service s3Service;

    @Autowired
    private DynamoDBService dynamoDBService;

    @Autowired
    private SnsService snsService;

    /**
     * Mark attendance for a student by submitting a face image.
     *
     * Flow:
     * 1. Upload captured image to S3 (attendance folder)
     * 2. Search face in Rekognition collection
     * 3. If match found → status = PRESENT, else ABSENT / UNKNOWN
     * 4. Save record to DynamoDB
     * 5. Send SNS notification
     * 6. Return result to frontend
     */
    @PostMapping("/mark")
    public ResponseEntity<ApiResponse<Map<String, Object>>> markAttendance(
            @Valid @RequestBody MarkAttendanceRequest request) {

        String timestamp = Instant.now().toString();
        log.info("Marking attendance at {}", timestamp);

        // 1. Upload to S3
        String fileName = "attendance-" + timestamp.replace(":", "-");
        String s3Key = s3Service.uploadBase64Image(request.getImageBase64(), "attendance", fileName);

        Map<String, Object> result = new HashMap<>();
        result.put("timestamp", timestamp);
        result.put("imageKey", s3Key);

        // 2. Search face
        Optional<String[]> matchOpt = rekognitionService.searchFace(request.getImageBase64());

        String studentId;
        String studentName = "Unknown";
        String status;
        double confidence;

        if (matchOpt.isPresent()) {
            String[] match = matchOpt.get();
            studentId = match[0];
            confidence = Double.parseDouble(match[1]);
            status = "PRESENT";

            result.put("studentId", studentId);
            result.put("confidence", confidence);
            result.put("status", "PRESENT");
            result.put("message", "Attendance marked successfully. Welcome!");

            log.info("Attendance marked PRESENT: studentId={}, confidence={}%", studentId, confidence);
        } else {
            studentId = "UNKNOWN";
            confidence = 0.0;
            status = "ABSENT";

            result.put("studentId", "UNKNOWN");
            result.put("confidence", 0.0);
            result.put("status", "ABSENT");
            result.put("message", "Face not recognized. Please register first or retake the photo.");

            log.info("Face not recognized — no match above threshold");
        }

        // 3. Save to DynamoDB
        AttendanceRecord record = AttendanceRecord.builder()
                .studentId(studentId)
                .studentName(studentName)
                .timestamp(timestamp)
                .status(status)
                .confidence(confidence)
                .imageKey(s3Key)
                .build();
        dynamoDBService.saveAttendanceRecord(record);

        // 4. Send SNS notification (only for recognized students)
        if (!"UNKNOWN".equals(studentId)) {
            try {
                snsService.publishAttendanceNotification(
                        studentId, studentName, status, confidence, timestamp);
            } catch (Exception e) {
                log.warn("SNS notification failed (non-fatal): {}", e.getMessage());
            }
        }

        return ResponseEntity.ok(ApiResponse.ok("Attendance processing complete", result));
    }

    /**
     * Get attendance records. Optional query param: ?studentId=STU001
     */
    @GetMapping("/list")
    public ResponseEntity<ApiResponse<List<AttendanceRecord>>> getAttendanceList(
            @RequestParam(required = false) String studentId) {

        List<AttendanceRecord> records = dynamoDBService.getAttendanceRecords(studentId);
        return ResponseEntity.ok(ApiResponse.ok("Attendance records retrieved", records));
    }

    /**
     * Get today's attendance summary statistics.
     */
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAttendanceSummary() {
        Map<String, Object> summary = dynamoDBService.getTodaySummary();
        return ResponseEntity.ok(ApiResponse.ok("Attendance summary retrieved", summary));
    }

    // ─── Inner request DTO ───────────────────────────────────────────────────

    public static class MarkAttendanceRequest {
        @NotBlank(message = "Image (Base64) is required")
        private String imageBase64;
        
        public String getImageBase64() { return imageBase64; }
        public void setImageBase64(String imageBase64) { this.imageBase64 = imageBase64; }
    }
}
