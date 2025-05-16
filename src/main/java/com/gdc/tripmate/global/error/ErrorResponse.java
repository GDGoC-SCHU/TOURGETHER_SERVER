package com.gdc.tripmate.global.error;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter @Setter
@Builder
public class ErrorResponse {
    private int status;
    private String message;
    private String detail;
    private String path;
    private LocalDateTime timestamp;

    // 간단한 오류 메시지만 필요한 경우를 위한 생성자
    public ErrorResponse(String message) {
        this.status = 400; // 기본값
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

    // 모든 필드를 설정하는 생성자
    public ErrorResponse(int status, String message, String detail, String path) {
        this.status = status;
        this.message = message;
        this.detail = detail;
        this.path = path;
        this.timestamp = LocalDateTime.now();
    }

    // Builder 패턴을 위한 정적 메서드
    public static ErrorResponseBuilder builder() {
        return new ErrorResponseBuilder();
    }

    // Builder 내부 클래스
    public static class ErrorResponseBuilder {
        private int status;
        private String message;
        private String detail;
        private String path;
        private LocalDateTime timestamp = LocalDateTime.now();

        ErrorResponseBuilder() {
        }

        public ErrorResponseBuilder status(int status) {
            this.status = status;
            return this;
        }

        public ErrorResponseBuilder message(String message) {
            this.message = message;
            return this;
        }

        public ErrorResponseBuilder detail(String detail) {
            this.detail = detail;
            return this;
        }

        public ErrorResponseBuilder path(String path) {
            this.path = path;
            return this;
        }

        public ErrorResponseBuilder timestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public ErrorResponse build() {
            return new ErrorResponse(status, message, detail, path);
        }
    }
}