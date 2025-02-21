package com.trinity.ctc.domain.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class AIRecommendationResponse {
    private Long userId;
    private List<Recommendation> recommendations;

    @Getter
    @AllArgsConstructor
    public static class Recommendation {
        private Long categoryId;
        private Long restaurantId;
        private double compositeScore;
    }
}
