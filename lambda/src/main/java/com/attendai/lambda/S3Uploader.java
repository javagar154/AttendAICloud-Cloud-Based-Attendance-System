package com.attendai.lambda;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.util.Base64;

/**
 * Helper class for uploading images to S3 within the Lambda function.
 */
public class S3Uploader {

    private static final Logger log = LoggerFactory.getLogger(S3Uploader.class);
    private static final String BUCKET_NAME =
            System.getenv().getOrDefault("S3_BUCKET_NAME", "attendai-faces-bucket");

    private final S3Client s3Client;

    public S3Uploader(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    /**
     * Decode Base64 image and upload to S3.
     *
     * @param imageBase64 Base64-encoded image (no data: prefix)
     * @param folder      S3 folder (e.g. "attendance")
     * @param fileName    file name without extension
     * @return full S3 key
     */
    public String uploadImage(String imageBase64, String folder, String fileName) {
        byte[] imageBytes = Base64.getDecoder().decode(imageBase64);
        String key = folder + "/" + fileName + ".jpg";

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(BUCKET_NAME)
                .key(key)
                .contentType("image/jpeg")
                .contentLength((long) imageBytes.length)
                .build();

        s3Client.putObject(request, RequestBody.fromBytes(imageBytes));
        log.info("Uploaded to S3: s3://{}/{}", BUCKET_NAME, key);
        return key;
    }
}
