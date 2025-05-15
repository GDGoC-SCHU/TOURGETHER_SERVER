package com.gdc.tripmate.domain.plan.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PlanInfoResponse {

	private String message;
	private String city;
	private String startDate;
	private String endDate;
}