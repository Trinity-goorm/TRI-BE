package com.trinity.ctc.domain.ai;


import com.trinity.ctc.domain.ai.dto.AIRecommendationRequest;
import com.trinity.ctc.domain.ai.dto.AIRecommendationResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

@Component
public class AIRecommendationClient {

    private static final String AI_SERVER_URL = "http://127.0.0.1:5000/recommend";
    private final RestTemplate restTemplate = new RestTemplate();

    public AIRecommendationResponse getRecommendations(AIRecommendationRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<AIRecommendationRequest> entity = new HttpEntity<>(request, headers);
        ResponseEntity<AIRecommendationResponse> response = restTemplate.exchange(
            AI_SERVER_URL, HttpMethod.POST, entity, AIRecommendationResponse.class);

        return response.getBody();
    }
}
