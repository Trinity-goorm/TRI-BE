package com.trinity.ctc.domain.ai;

import com.trinity.ctc.domain.ai.dto.AIRecommendationRequest;
import com.trinity.ctc.domain.ai.dto.AIRecommendationResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AIRecommendationClientDumy {

    public AIRecommendationResponse getRecommendations(AIRecommendationRequest request) {
        return new AIRecommendationResponse(
            request.getUserId(),
            List.of(
                new AIRecommendationResponse.Recommendation(11L, 37L, 4.886),
                new AIRecommendationResponse.Recommendation(12L, 21L, 4.88),
                new AIRecommendationResponse.Recommendation(11L, 67L, 4.871)
            )
        );
    }
}
