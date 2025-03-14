package com.trinity.ctc.domain.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trinity.ctc.domain.ai.dto.AIRecommendationRequest;
import com.trinity.ctc.domain.ai.dto.AIRecommendationResponse;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AIRecommendationClientDumy {

    public AIRecommendationResponse getRecommendations(AIRecommendationRequest request) {
        AIRecommendationResponse recommendationResponse = new AIRecommendationResponse();
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            File file = new ClassPathResource("recommend-dummy-data.json").getFile();
            JsonNode node = objectMapper.readTree(file);

            List<AIRecommendationResponse.Recommendation> recommendations = new ArrayList<>();

            node.get("recommendations").forEach(recommendationNode -> {
                AIRecommendationResponse.Recommendation recommendation
                        = new AIRecommendationResponse.Recommendation(
                        recommendationNode.get("category_id").asLong(),
                        recommendationNode.get("restaurant_id").asLong(),
                        recommendationNode.get("composite_score").asDouble());
                recommendations.add(recommendation);
            });

            Long userId = 1L;
            recommendationResponse = new AIRecommendationResponse(userId, recommendations);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return recommendationResponse;
    }
}
