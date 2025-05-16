package com.gdc.tripmate.domain.tag.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TagRequest {

	private List<String> tags;
}