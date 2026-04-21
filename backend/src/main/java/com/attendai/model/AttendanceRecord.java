package com.attendai.model;

/**
 * DynamoDB attendance record model.
 * Table: AttendanceRecords
 * PK: studentId (String)
 * SK: timestamp (String — ISO-8601)
 */
public class AttendanceRecord {
    private String studentId;
    private String studentName;
    private String timestamp;
    private String status;       // "PRESENT" or "ABSENT"
    private Double confidence;   // Rekognition confidence score
    private String imageKey;     // S3 key of the captured image

    public AttendanceRecord() {}

    public AttendanceRecord(String studentId, String studentName, String timestamp, String status, Double confidence, String imageKey) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.timestamp = timestamp;
        this.status = status;
        this.confidence = confidence;
        this.imageKey = imageKey;
    }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Double getConfidence() { return confidence; }
    public void setConfidence(Double confidence) { this.confidence = confidence; }

    public String getImageKey() { return imageKey; }
    public void setImageKey(String imageKey) { this.imageKey = imageKey; }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String studentId;
        private String studentName;
        private String timestamp;
        private String status;
        private Double confidence;
        private String imageKey;

        public Builder studentId(String studentId) { this.studentId = studentId; return this; }
        public Builder studentName(String studentName) { this.studentName = studentName; return this; }
        public Builder timestamp(String timestamp) { this.timestamp = timestamp; return this; }
        public Builder status(String status) { this.status = status; return this; }
        public Builder confidence(Double confidence) { this.confidence = confidence; return this; }
        public Builder imageKey(String imageKey) { this.imageKey = imageKey; return this; }

        public AttendanceRecord build() {
            return new AttendanceRecord(studentId, studentName, timestamp, status, confidence, imageKey);
        }
    }
}
