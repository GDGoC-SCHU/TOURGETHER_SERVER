package com.gdc.tripmate.domain.plan.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlanInfoRequest {

	private String city;
	private String startDate;
	private String endDate;
}