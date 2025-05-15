package com.gdc.tripmate.domain.plan.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.gdc.tripmate.domain.plan.dto.PlanInfoRequest;
import com.gdc.tripmate.domain.plan.service.PlanInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/plan")
@RequiredArgsConstructor
public class PlanInfoContoller {

	private final PlanInfoService planInfoService;

	@PostMapping
	public JsonNode sendPlanToAI(@RequestBody PlanInfoRequest request) {
		return planInfoService.handlePlanInfo(request);
	}
}