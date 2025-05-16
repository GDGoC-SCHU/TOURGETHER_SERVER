package com.gdc.tripmate.domain.tag.controller;

import com.gdc.tripmate.domain.tag.dto.request.TagDto;
import com.gdc.tripmate.domain.tag.service.TagService;
import com.gdc.tripmate.global.error.ErrorResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


/**
 * 태그 관리 API를 처리하는 관리자용 컨트롤러
 */
@RestController
@RequestMapping("/api/admin/tags")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class TagAdminController {

    private final TagService tagService;

    /**
     * 모든 태그 조회 API
     */
    @GetMapping
    public ResponseEntity<List<TagDto>> getAllTags() {
        List<TagDto> tags = tagService.getAllTags();
        return ResponseEntity.ok(tags);
    }

    /**
     * 태그 단일 조회 API
     */
    @GetMapping("/{id}")
    public ResponseEntity<TagDto> getTagById(@PathVariable Long id) {
        try {
            TagDto tag = tagService.getTagById(id);
            return ResponseEntity.ok(tag);
        } catch (Exception e) {
            log.error("태그 조회 오류 발생: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 태그 생성 API
     */
    @PostMapping
    public ResponseEntity<?> createTag(@RequestBody TagDto tagDto) {
        try {
            TagDto createdTag = tagService.createTag(tagDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdTag);
        } catch (IllegalArgumentException e) {
            log.error("태그 생성 오류 발생: {}", e.getMessage());
            ErrorResponse errorResponse = ErrorResponse.builder()
                    .status(HttpStatus.BAD_REQUEST.value())
                    .message(e.getMessage())
                    .build();
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            log.error("태그 생성 중 내부 오류 발생: {}", e.getMessage());
            ErrorResponse errorResponse = ErrorResponse.builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("태그 생성 중 오류가 발생했습니다.")
                    .detail(e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 태그 업데이트 API
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateTag(@PathVariable Long id, @RequestBody TagDto tagDto) {
        try {
            TagDto updatedTag = tagService.updateTag(id, tagDto);
            return ResponseEntity.ok(updatedTag);
        } catch (IllegalArgumentException e) {
            log.error("태그 업데이트 오류 발생: {}", e.getMessage());
            ErrorResponse errorResponse = ErrorResponse.builder()
                    .status(HttpStatus.BAD_REQUEST.value())
                    .message(e.getMessage())
                    .build();
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            log.error("태그 업데이트 중 내부 오류 발생: {}", e.getMessage());
            ErrorResponse errorResponse = ErrorResponse.builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("태그 업데이트 중 오류가 발생했습니다.")
                    .detail(e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 태그 삭제 API
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTag(@PathVariable Long id) {
        try {
            tagService.deleteTag(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("태그 삭제 중 오류 발생: {}", e.getMessage());
            ErrorResponse errorResponse = ErrorResponse.builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("태그 삭제 중 오류가 발생했습니다.")
                    .detail(e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}