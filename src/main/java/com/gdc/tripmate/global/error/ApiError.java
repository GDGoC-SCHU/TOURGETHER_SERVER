package com.gdc.tripmate.global.error;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

/**
 * API 오류 응답을 위한 DTO
 */
@Getter
public class ApiError {

    private final HttpStatus status;
    private final String message;
    private final String path;
    private final LocalDateTime timestamp;

    public ApiError(HttpStatus status, String message, String path) {
        this.status = status;
        this.message = message;
        this.path = path;
        this.timestamp = LocalDateTime.now();
    }
}