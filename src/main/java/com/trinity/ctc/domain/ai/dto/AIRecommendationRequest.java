package com.trinity.ctc.domain.ai.dto;


import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AIRecommendationRequest {
    private Long userId;
    private List<String> preferredCategories;
}
