package com.gdc.tripmate.domain.tag.service;

import com.gdc.tripmate.domain.tag.dto.TagRequest;
import com.gdc.tripmate.domain.tag.dto.TagResponse;


public interface TagService {

	TagResponse processTag(TagRequest request);
}