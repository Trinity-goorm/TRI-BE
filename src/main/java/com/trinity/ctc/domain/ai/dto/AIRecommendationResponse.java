package com.trinity.ctc.domain.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
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
