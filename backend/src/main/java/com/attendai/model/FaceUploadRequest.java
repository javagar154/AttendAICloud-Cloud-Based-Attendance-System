package com.attendai.model;

import jakarta.validation.constraints.NotBlank;

/**
 * Request body for face upload and registration.
 */
public class FaceUploadRequest {
    @NotBlank(message = "Student ID is required")
    private String studentId;

    @NotBlank(message = "Student name is required")
    private String studentName;

    private String email;

    private String department;

    @NotBlank(message = "Image (Base64) is required")
    private String imageBase64;  // JPEG/PNG image encoded as Base64 (without data: prefix)

    public FaceUploadRequest() {}

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getImageBase64() { return imageBase64; }
    public void setImageBase64(String imageBase64) { this.imageBase64 = imageBase64; }
}
