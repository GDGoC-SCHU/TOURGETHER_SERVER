// AuthResult.java - DTO 객체
package com.gdc.tripmate.domain.user.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * 인증 결과를 나타내는 DTO 객체
 * 서비스 계층 간 데이터 전달에 사용
 */
@Getter
@Builder
public class AuthResult {
    private boolean success;
    private String accessToken;
    private String email;
    private String errorMessage;
}