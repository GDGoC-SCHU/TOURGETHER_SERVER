package com.gdc.tripmate.domain.plan.service;


import com.fasterxml.jackson.databind.JsonNode;
import com.gdc.tripmate.domain.plan.dto.PlanInfoRequest;

public interface PlanInfoService {

	JsonNode handlePlanInfo(PlanInfoRequest request);
}