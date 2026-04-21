package com.attendai.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Custom application exception for AttendAI.
 * Use for business-logic errors (e.g., face not found, S3 error).
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class AttendAiException extends RuntimeException {

    private final String errorCode;

    public AttendAiException(String message) {
        super(message);
        this.errorCode = "ATTENDAI_ERROR";
    }

    public AttendAiException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public AttendAiException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "ATTENDAI_ERROR";
    }

    public String getErrorCode() {
        return errorCode;
    }
}
