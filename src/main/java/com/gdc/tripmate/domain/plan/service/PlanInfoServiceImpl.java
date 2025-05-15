package com.gdc.tripmate.domain.plan.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gdc.tripmate.domain.plan.dto.PlanInfoRequest;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class PlanInfoServiceImpl implements PlanInfoService {

	@Override
	public JsonNode handlePlanInfo(PlanInfoRequest request) {

		try {

			// 요청 파라미터 확인용
			System.out.println("🌐 [프론트 요청]");
			System.out.println("도시: " + request.getCity());
			System.out.println("시작날짜: " + request.getStartDate());
			System.out.println("끝날짜: " + request.getEndDate());
			// 1. Flask에 보낼 JSON 구성
			Map<String, String> aiRequest = new HashMap<>();
			aiRequest.put("city", request.getCity());
			aiRequest.put("startDate", request.getStartDate());
			aiRequest.put("endDate", request.getEndDate());

			// 2. 요청 헤더
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<Map<String, String>> entity = new HttpEntity<>(aiRequest, headers);

			// 3. Flask 서버 URL
			String flaskUrl = "http://220.69.209.244:5001/ai/generate";

			// 4. POST 요청
			RestTemplate restTemplate = new RestTemplate();
			ResponseEntity<String> flaskResponse = restTemplate.postForEntity(flaskUrl, entity,
					String.class);

			// Flask 응답 확인용
			System.out.println("🤖 [AI 응답]");
			System.out.println(flaskResponse.getBody());

			// 5. Flask 응답을 JSON으로 파싱해서 리턴
			ObjectMapper mapper = new ObjectMapper();
			JsonNode result = mapper.readTree(flaskResponse.getBody());

			// 📤 프론트로 보낼 응답 확인
			System.out.println("📤 [프론트로 전달될 응답]");
			System.out.println(result);

			return result;


		} catch (Exception e) {
			// 6. 에러 발생 시 JSON 형태로 반환
			ObjectMapper mapper = new ObjectMapper();
			ObjectNode errorNode = mapper.createObjectNode();
			errorNode.put("error", "AI 서버 요청 실패: " + e.getMessage());
			return errorNode;
		}
	}


}