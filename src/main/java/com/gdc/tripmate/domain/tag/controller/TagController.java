package com.gdc.tripmate.domain.tag.controller;

import com.gdc.tripmate.domain.tag.service.TagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 태그 관련 API를 처리하는 일반 컨트롤러
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class TagController {

    private final TagService tagService;

    /**
     * 카테고리별 태그 목록 조회 API
     */
    @GetMapping("/tags")
    public ResponseEntity<Map<String, List<String>>> getAllCategorizedTags() {
        Map<String, List<String>> tags = tagService.getAllCategorizedTags();
        return ResponseEntity.ok(tags);
    }

    /**
     * 특정 카테고리의 태그 목록 조회 API
     */
    @GetMapping("/tags/category/{category}")
    public ResponseEntity<List<String>> getTagsByCategory(@PathVariable String category) {
        Map<String, List<String>> allTags = tagService.getAllCategorizedTags();
        List<String> categoryTags = allTags.getOrDefault(category.toUpperCase(), List.of());
        return ResponseEntity.ok(categoryTags);
    }
}