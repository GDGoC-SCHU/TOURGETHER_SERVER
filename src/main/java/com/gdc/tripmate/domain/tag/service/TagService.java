package com.gdc.tripmate.domain.tag.service;


import com.gdc.tripmate.domain.tag.dto.request.TagDto;
import java.util.List;
import java.util.Map;

/**
 * 태그 관련 비즈니스 로직을 정의하는 인터페이스
 */
public interface TagService {

	/**
	 * 모든 태그 조회
	 *
	 * @return 태그 DTO 목록
	 */
	List<TagDto> getAllTags();

	/**
	 * 카테고리별 태그 목록 조회
	 *
	 * @return 카테고리별 태그 목록 맵
	 */
	Map<String, List<String>> getAllCategorizedTags();

	/**
	 * 특정 카테고리의 태그 목록 조회
	 *
	 * @param category 태그 카테고리
	 * @return 태그 DTO 목록
	 */
	List<TagDto> getTagsByCategory(String category);

	/**
	 * 태그 단일 조회
	 *
	 * @param id 태그 ID
	 * @return 태그 DTO
	 */
	TagDto getTagById(Long id);

	/**
	 * 태그 생성
	 *
	 * @param tagDto 생성할 태그 정보
	 * @return 생성된 태그 DTO
	 */
	TagDto createTag(TagDto tagDto);

	/**
	 * 태그 업데이트
	 *
	 * @param id     업데이트할 태그 ID
	 * @param tagDto 업데이트할 태그 정보
	 * @return 업데이트된 태그 DTO
	 */
	TagDto updateTag(Long id, TagDto tagDto);

	/**
	 * 태그 삭제
	 *
	 * @param id 삭제할 태그 ID
	 */
	void deleteTag(Long id);

	/**
	 * 태그 존재 여부 확인
	 *
	 * @param name 확인할 태그 이름
	 * @return 존재 여부
	 */
	boolean existsByName(String name);
}