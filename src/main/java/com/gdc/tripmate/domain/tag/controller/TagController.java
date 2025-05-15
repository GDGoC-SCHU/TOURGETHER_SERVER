package com.gdc.tripmate.domain.tag.controller;

import com.gdc.tripmate.domain.tag.dto.TagRequest;
import com.gdc.tripmate.domain.tag.dto.TagResponse;
import com.gdc.tripmate.domain.tag.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tag")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    @PostMapping
    public TagResponse receiveTags(@RequestBody TagRequest request) {
        return tagService.processTag(request);
    }
}