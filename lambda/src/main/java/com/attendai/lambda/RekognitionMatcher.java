package com.attendai.lambda;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.rekognition.RekognitionClient;
import software.amazon.awssdk.services.rekognition.model.*;

import java.util.Base64;
import java.util.List;
import java.util.Optional;

/**
 * Helper class for searching faces in Rekognition within the Lambda function.
 */
public class RekognitionMatcher {

    private static final Logger log = LoggerFactory.getLogger(RekognitionMatcher.class);
    private static final String COLLECTION_ID =
            System.getenv().getOrDefault("REKOGNITION_COLLECTION_ID", "attendai-faces");
    private static final float CONFIDENCE_THRESHOLD =
            Float.parseFloat(System.getenv().getOrDefault("REKOGNITION_CONFIDENCE_THRESHOLD", "90.0"));

    private final RekognitionClient rekognitionClient;

    public RekognitionMatcher(RekognitionClient rekognitionClient) {
        this.rekognitionClient = rekognitionClient;
    }

    /**
     * Search for a face in the Rekognition collection.
     *
     * @param imageBase64 Base64-encoded face image
     * @return Optional containing [studentId, confidenceScore] if match found
     */
    public Optional<String[]> searchFace(String imageBase64) {
        byte[] imageBytes = Base64.getDecoder().decode(imageBase64);

        SearchFacesByImageRequest request = SearchFacesByImageRequest.builder()
                .collectionId(COLLECTION_ID)
                .image(Image.builder()
                        .bytes(SdkBytes.fromByteArray(imageBytes))
                        .build())
                .faceMatchThreshold(CONFIDENCE_THRESHOLD)
                .maxFaces(1)
                .build();

        try {
            SearchFacesByImageResponse response = rekognitionClient.searchFacesByImage(request);
            List<FaceMatch> matches = response.faceMatches();

            if (matches.isEmpty()) {
                log.info("No matching face found above {}% threshold", CONFIDENCE_THRESHOLD);
                return Optional.empty();
            }

            FaceMatch topMatch = matches.get(0);
            String studentId = topMatch.face().externalImageId();
            String confidence = String.valueOf(topMatch.similarity());

            log.info("Face matched: studentId={}, confidence={}%", studentId, confidence);
            return Optional.of(new String[]{studentId, confidence});

        } catch (InvalidParameterException e) {
            // No face detected in the image
            log.warn("No face detected in image: {}", e.getMessage());
            return Optional.empty();
        }
    }
}
