package com.gdc.tripmate.domain.tag.service;

import com.gdc.tripmate.domain.tag.dto.TagRequest;
import com.gdc.tripmate.domain.tag.dto.TagResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TagServiceImpl implements TagService {

	@Override
	public TagResponse processTag(TagRequest request) {
		log.info("수신된 태그 목록: {}", request.getTags());
		return new TagResponse("태그 처리 완료", request.getTags());
	}
}