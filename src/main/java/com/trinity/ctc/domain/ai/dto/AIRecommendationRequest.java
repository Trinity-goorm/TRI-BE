package com.trinity.ctc.domain.ai.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class AIRecommendationRequest {
    private Long userId;
    private List<String> preferredCategories;
}
