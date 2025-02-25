package com.trinity.ctc.domain.ai.dto;


import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AIRecommendationRequest {
    private Long userId;
    private List<String> preferredCategories;
    private int minPrice;
    private int maxPrice;
    private List<Long> likeList;      // 사용자가 찜한 식당 ID 리스트
    private List<String> searchHistory; // 사용자의 최근 검색 기록
}
