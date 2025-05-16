package com.gdc.tripmate.global.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * 파일 업로드 관련 유틸리티 클래스
 */
@Component
@Slf4j
public class FileUploadUtil {

    @Value("${app.file.upload-dir:uploads}")
    private String uploadDir;

    @Value("${app.file.base-url:http://localhost:8080/files}")
    private String baseUrl;

    /**
     * 파일 업로드 처리
     * 
     * @param file 업로드할 파일
     * @param subdirectory 저장할 하위 디렉토리 (옵션)
     * @return 파일 URL
     */
    public String uploadFile(MultipartFile file, String subdirectory) {
        try {
            // 파일이 비어있는지 확인
            if (file.isEmpty()) {
                throw new IllegalArgumentException("빈 파일입니다.");
            }

            // 원본 파일명에서 확장자 추출
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            // 유니크한 파일명 생성
            String newFilename = UUID.randomUUID().toString() + extension;

            // 저장 경로 설정
            String directoryPath = uploadDir;
            if (subdirectory != null && !subdirectory.isEmpty()) {
                directoryPath = uploadDir + File.separator + subdirectory;
            }

            // 디렉토리가 없으면 생성
            Path directory = Paths.get(directoryPath);
            if (!Files.exists(directory)) {
                Files.createDirectories(directory);
            }

            // 파일 저장
            Path filePath = Paths.get(directoryPath, newFilename);
            Files.write(filePath, file.getBytes());

            // 파일 URL 생성
            String fileUrl = baseUrl;
            if (subdirectory != null && !subdirectory.isEmpty()) {
                fileUrl += "/" + subdirectory;
            }
            fileUrl += "/" + newFilename;

            return fileUrl;
        } catch (IOException e) {
            log.error("파일 업로드 중 오류 발생", e);
            throw new RuntimeException("파일 업로드에 실패했습니다: " + e.getMessage());
        }
    }
}