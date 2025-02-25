package com.trinity.ctc.domain.ai;


import com.trinity.ctc.domain.ai.dto.AIRecommendationRequest;
import com.trinity.ctc.domain.ai.dto.AIRecommendationResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

@Component
public class AIRecommendationClient {

    @Value("${ai.api.url}")
    private String AI_SERVER_URL;

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
