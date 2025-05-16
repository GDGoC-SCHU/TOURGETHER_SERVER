package com.gdc.tripmate.domain.tag.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class TagResponse {

	private String message;
	private List<String> tags;
}