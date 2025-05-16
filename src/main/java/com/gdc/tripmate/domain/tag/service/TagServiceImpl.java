package com.gdc.tripmate.domain.tag.service;

import com.gdc.tripmate.domain.tag.dto.request.TagDto;
import com.gdc.tripmate.domain.tag.entity.Tag;
import com.gdc.tripmate.domain.tag.repository.TagRepository;
import com.gdc.tripmate.global.error.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 태그 관련 비즈니스 로직을 처리하는 서비스 구현체
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TagServiceImpl implements TagService {

    private final TagRepository tagRepository;

    /**
     * 모든 태그 조회
     */
    @Override
    @Transactional(readOnly = true)
    public List<TagDto> getAllTags() {
        return tagRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 카테고리별 태그 목록 조회
     */
    @Override
    @Transactional(readOnly = true)
    public Map<String, List<String>> getAllCategorizedTags() {
        List<Tag> allTags = tagRepository.findAll();
        
        // 태그를 카테고리별로 그룹화
        Map<String, List<String>> categorizedTags = new HashMap<>();
        
        // 태그를 카테고리별로 그룹화
        Map<String, List<Tag>> tagsByCategory = allTags.stream()
                .collect(Collectors.groupingBy(Tag::getCategory));
        
        // 각 카테고리별로 태그 이름만 추출
        tagsByCategory.forEach((category, tags) -> {
            List<String> tagNames = tags.stream()
                    .map(Tag::getName)
                    .collect(Collectors.toList());
            categorizedTags.put(category, tagNames);
        });
        
        return categorizedTags;
    }

    /**
     * 특정 카테고리의 태그 목록 조회
     */
    @Override
    @Transactional(readOnly = true)
    public List<TagDto> getTagsByCategory(String category) {
        return tagRepository.findAllByCategory(category).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 태그 단일 조회
     */
    @Override
    @Transactional(readOnly = true)
    public TagDto getTagById(Long id) {
        return tagRepository.findById(id)
                .map(this::convertToDto)
                .orElseThrow(() -> new ResourceNotFoundException("태그를 찾을 수 없습니다. ID: " + id));
    }

    /**
     * 태그 생성
     */
    @Override
    @Transactional
    public TagDto createTag(TagDto tagDto) {
        // 이름 중복 체크
        if (tagRepository.existsByName(tagDto.getName())) {
            throw new IllegalArgumentException("이미 존재하는 태그 이름입니다: " + tagDto.getName());
        }
        
        // 태그 생성 및 저장
        Tag tag = new Tag(tagDto.getName(), tagDto.getCategory());
        Tag savedTag = tagRepository.save(tag);
        
        return convertToDto(savedTag);
    }

    /**
     * 태그 업데이트
     */
    @Override
    @Transactional
    public TagDto updateTag(Long id, TagDto tagDto) {
        // 태그 존재 여부 확인
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("태그를 찾을 수 없습니다. ID: " + id));
        
        // 이름 변경 시 중복 체크
        if (!tag.getName().equals(tagDto.getName()) && tagRepository.existsByName(tagDto.getName())) {
            throw new IllegalArgumentException("이미 존재하는 태그 이름입니다: " + tagDto.getName());
        }
        
        // 새 태그 생성 (불변 객체이므로)
        Tag updatedTag = new Tag(tagDto.getName(), tagDto.getCategory());
        // ID 설정
        updatedTag = tagRepository.save(updatedTag);
        
        return convertToDto(updatedTag);
    }

    /**
     * 태그 삭제
     */
    @Override
    @Transactional
    public void deleteTag(Long id) {
        // 태그 존재 여부 확인
        if (!tagRepository.existsById(id)) {
            throw new ResourceNotFoundException("태그를 찾을 수 없습니다. ID: " + id);
        }
        
        // 태그 삭제
        tagRepository.deleteById(id);
    }

    /**
     * 태그 존재 여부 확인
     */
    @Override
    @Transactional(readOnly = true)
    public boolean existsByName(String name) {
        return tagRepository.existsByName(name);
    }

    /**
     * Tag 엔티티를 TagDto로 변환
     */
    private TagDto convertToDto(Tag tag) {
        return TagDto.builder()
                .id(tag.getId())
                .name(tag.getName())
                .category(tag.getCategory())
                .build();
    }
}