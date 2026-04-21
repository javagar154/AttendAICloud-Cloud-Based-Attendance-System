package com.attendai.controller;

import com.attendai.model.ApiResponse;
import com.attendai.model.FaceUploadRequest;
import com.attendai.service.RekognitionService;
import com.attendai.service.S3Service;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Face management controller.
 * POST /api/face/upload            — Register a student's face into Rekognition collection
 * POST /api/face/collection/create — Create a new Rekognition collection (admin)
 * All endpoints require a valid JWT.
 */
@RestController
@RequestMapping("/api/face")
public class FaceController {

    private static final Logger log = LoggerFactory.getLogger(FaceController.class);

    @Autowired
    private RekognitionService rekognitionService;

    @Autowired
    private S3Service s3Service;

    /**
     * Register a new student face.
     * 1. Upload image to S3 under registered/<studentId>-<timestamp>.jpg
     * 2. Index face in Rekognition collection (externalImageId = studentId)
     * 3. Return the assigned faceId
     */
    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadFace(
            @Valid @RequestBody FaceUploadRequest request) {

        log.info("Registering face for student: {}", request.getStudentId());

        // 1. Upload to S3
        String fileName = request.getStudentId() + "-" + Instant.now().toEpochMilli();
        String s3Key = s3Service.uploadBase64Image(request.getImageBase64(), "registered", fileName);

        // 2. Index in Rekognition
        String faceId = rekognitionService.indexFace(request.getImageBase64(), request.getStudentId());

        Map<String, String> responseData = new HashMap<>();
        responseData.put("studentId", request.getStudentId());
        responseData.put("faceId", faceId);
        responseData.put("s3Key", s3Key);
        responseData.put("message", "Face registered successfully");

        return ResponseEntity.ok(ApiResponse.ok(
                "Student face registered in Rekognition collection", responseData));
    }

    /**
     * Create a new Rekognition face collection.
     * Should be called once during initial setup.
     */
    @PostMapping("/collection/create")
    public ResponseEntity<ApiResponse<Map<String, String>>> createCollection(
            @Valid @RequestBody CreateCollectionRequest request) {

        String arn = rekognitionService.createCollection(request.getCollectionId());

        Map<String, String> data = new HashMap<>();
        data.put("collectionId", request.getCollectionId());
        data.put("arn", arn);

        return ResponseEntity.ok(ApiResponse.ok("Collection created successfully", data));
    }

    // ─── Inner request DTO ───────────────────────────────────────────────────

    public static class CreateCollectionRequest {
        @NotBlank(message = "Collection ID is required")
        private String collectionId;
        
        public String getCollectionId() { return collectionId; }
        public void setCollectionId(String collectionId) { this.collectionId = collectionId; }
    }
}
