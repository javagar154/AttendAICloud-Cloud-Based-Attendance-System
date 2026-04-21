package com.attendai.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.util.Base64;

/**
 * Service for interacting with Amazon S3.
 * Handles uploading face images captured during attendance and registration.
 */
@Service
public class S3Service {

    private static final Logger log = LoggerFactory.getLogger(S3Service.class);

    @Autowired
    private S3Client s3Client;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Value("${aws.dummy-mode:false}")
    private boolean dummyMode;

    /**
     * Upload a Base64-encoded image to S3.
     *
     * @param imageBase64 Base64 string of the image (without data: prefix)
     * @param folder      S3 folder prefix (e.g., "registered", "attendance")
     * @param fileName    desired file name (without extension)
     * @return the full S3 key (path) of the uploaded object
     */
    public String uploadBase64Image(String imageBase64, String folder, String fileName) {
        String key = folder + "/" + fileName + ".jpg";
        
        // Dummy mode check for local testing
        if (dummyMode || "fake_access_key".equals(System.getenv("AWS_ACCESS_KEY_ID"))) {
            log.info("[DUMMY MODE] Mocking S3 upload for key: {}", key);
            return key;
        }

        byte[] imageBytes = Base64.getDecoder().decode(imageBase64);

        PutObjectRequest putRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType("image/jpeg")
                .contentLength((long) imageBytes.length)
                .build();

        PutObjectResponse response = s3Client.putObject(putRequest, RequestBody.fromBytes(imageBytes));
        log.info("Uploaded image to S3: s3://{}/{} (ETag: {})", bucketName, key, response.eTag());
        return key;
    }

    /**
     * Get the full S3 URI for a given key.
     */
    public String getS3Uri(String key) {
        return "s3://" + bucketName + "/" + key;
    }

    /**
     * Get the public HTTPS URL for a given key (bucket must allow public access or use pre-signed URLs).
     */
    public String getObjectUrl(String key) {
        return "https://" + bucketName + ".s3.amazonaws.com/" + key;
    }
}
