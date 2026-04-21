package com.attendai.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.rekognition.RekognitionClient;
import software.amazon.awssdk.services.rekognition.model.*;

import java.util.Base64;
import java.util.List;
import java.util.Optional;

/**
 * Service for AWS Rekognition operations.
 * - Index faces into a collection (registration)
 * - Search for matching faces (attendance marking)
 */
@Service
public class RekognitionService {

    private static final Logger log = LoggerFactory.getLogger(RekognitionService.class);

    @Autowired
    private RekognitionClient rekognitionClient;

    @Value("${aws.rekognition.collection-id}")
    private String collectionId;

    @Value("${aws.rekognition.confidence-threshold}")
    private float confidenceThreshold;

    @Value("${aws.dummy-mode:false}")
    private boolean dummyMode;

    /**
     * Create a new Rekognition Face Collection.
     * Call this once during initial setup.
     *
     * @param colId collection ID to create (uses config value if null)
     * @return ARN of the created collection
     */
    public String createCollection(String colId) {
        String id = (colId != null && !colId.isBlank()) ? colId : collectionId;
        CreateCollectionRequest request = CreateCollectionRequest.builder()
                .collectionId(id)
                .build();
        CreateCollectionResponse response = rekognitionClient.createCollection(request);
        log.info("Created Rekognition collection: {} (ARN: {})", id, response.collectionArn());
        return response.collectionArn();
    }

    /**
     * Index (register) a face into the collection.
     * Associates the face with the given externalImageId (studentId).
     *
     * @param imageBase64 Base64-encoded image
     * @param studentId   used as externalImageId for later retrieval
     * @return FaceId assigned by Rekognition
     */
    public String indexFace(String imageBase64, String studentId) {
        // Dummy mode check for local testing
        if (dummyMode || "fake_access_key".equals(System.getenv("AWS_ACCESS_KEY_ID"))) {
            log.info("[DUMMY MODE] Mocking Rekognition indexFace for student: {}", studentId);
            return "mock-face-id-" + studentId;
        }

        byte[] imageBytes = Base64.getDecoder().decode(imageBase64);

        IndexFacesRequest request = IndexFacesRequest.builder()
                .collectionId(collectionId)
                .image(Image.builder()
                        .bytes(SdkBytes.fromByteArray(imageBytes))
                        .build())
                .externalImageId(studentId)  // link faceId → studentId
                .detectionAttributes(Attribute.ALL)
                .maxFaces(1)
                .qualityFilter(QualityFilter.AUTO)
                .build();

        IndexFacesResponse response = rekognitionClient.indexFaces(request);

        if (response.faceRecords().isEmpty()) {
            log.warn("No face detected in the image for studentId: {}", studentId);
            throw new RuntimeException("No face detected in the provided image. Please use a clear frontal photo.");
        }

        String faceId = response.faceRecords().get(0).face().faceId();
        log.info("Indexed face for student {} — faceId: {}", studentId, faceId);
        return faceId;
    }

    /**
     * Search for a face in the collection.
     * Returns the matched student ID and confidence score.
     *
     * @param imageBase64 Base64-encoded image to search
     * @return Optional of a String array: [studentId, confidenceScore]
     */
    public Optional<String[]> searchFace(String imageBase64) {
        // Dummy mode check for local testing
        if (dummyMode || "fake_access_key".equals(System.getenv("AWS_ACCESS_KEY_ID"))) {
            log.info("[DUMMY MODE] Mocking Rekognition searchFace");
            return Optional.of(new String[]{"23105019", "99.9"}); // Returns a test ID
        }

        byte[] imageBytes = Base64.getDecoder().decode(imageBase64);

        SearchFacesByImageRequest request = SearchFacesByImageRequest.builder()
                .collectionId(collectionId)
                .image(Image.builder()
                        .bytes(SdkBytes.fromByteArray(imageBytes))
                        .build())
                .faceMatchThreshold(confidenceThreshold)
                .maxFaces(1)
                .build();

        try {
            SearchFacesByImageResponse response = rekognitionClient.searchFacesByImage(request);
            List<FaceMatch> matches = response.faceMatches();

            if (matches.isEmpty()) {
                log.info("No matching face found in collection (threshold: {}%)", confidenceThreshold);
                return Optional.empty();
            }

            FaceMatch topMatch = matches.get(0);
            String studentId = topMatch.face().externalImageId();
            String confidence = String.valueOf(topMatch.similarity());
            log.info("Face matched: studentId={}, confidence={}%", studentId, confidence);
            return Optional.of(new String[]{studentId, confidence});

        } catch (InvalidParameterException e) {
            log.warn("No face detected in the submitted image: {}", e.getMessage());
            return Optional.empty();
        }
    }
}
