package com.gdc.tripmate.domain.user.status;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Status {
	PENDING("PENDING"), // 보류
	ACTIVE("ACTIVE"), // 활성화중
	SUSPENDED("SUSPENDED"), // 정지된
	DELETED("DELETED"), // 삭제된
	INACTIVE("INAVTIVE"); // 비활성화, 로그아웃 등등

	private final String message;
}
