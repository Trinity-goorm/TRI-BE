package com.trinity.ctc.domain.restaurant.service;

import com.trinity.ctc.domain.ai.AIRecommendationClient;
import com.trinity.ctc.domain.ai.dto.AIRecommendationRequest;
import com.trinity.ctc.domain.ai.dto.AIRecommendationResponse;
import com.trinity.ctc.domain.like.repository.LikeRepository;
import com.trinity.ctc.domain.restaurant.dto.RestaurantPreviewResponse;
import com.trinity.ctc.domain.restaurant.entity.Restaurant;
import com.trinity.ctc.domain.restaurant.repository.RestaurantRepository;
import com.trinity.ctc.domain.user.entity.User;
import com.trinity.ctc.domain.user.entity.UserPreference;
import com.trinity.ctc.domain.user.repository.UserPreferenceRepository;
import com.trinity.ctc.kakao.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RestaurantRecommendationService {

    private final AIRecommendationClient aiRecommendationClient;
    private final RestaurantRepository restaurantRepository;
    private final LikeRepository likeRepository;
    private final UserRepository userRepository;
    private final UserPreferenceRepository userPreferenceRepository;

    @Transactional()
    public List<RestaurantPreviewResponse> getRecommendedRestaurants(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("해당 유저를 찾을 수 없습니다. ID: " + userId));

        UserPreference userPreference = userPreferenceRepository.findByUserId(userId);
        List<String> preferredCategories = userPreference.getUserPReferenceCategoryList().stream()
            .map(upc -> upc.getCategory().getName())
            .collect(Collectors.toList());

//        AIRecommendationRequest request = new AIRecommendationRequest(user.getId(), preferredCategories);
//        AIRecommendationResponse aiResponse = aiRecommendationClient.getRecommendations(request);
        // AI 서버 대신 Mock 데이터를 사용
        AIRecommendationRequest request = new AIRecommendationRequest(user.getId(), List.of("중식", "일식", "양식"));
        AIRecommendationResponse aiResponse = aiRecommendationClient.getRecommendations(request);

        return aiResponse.getRecommendations().stream()
            .map(recommendation -> {
                Restaurant restaurant = restaurantRepository.findById(recommendation.getRestaurantId())
                    .orElseThrow(() -> new IllegalArgumentException("해당 식당을 찾을 수 없습니다. ID: " + recommendation.getRestaurantId()));

                boolean isWishlisted = likeRepository.existsByUserAndRestaurant(user, restaurant);

                return RestaurantPreviewResponse.fromEntity(restaurant, isWishlisted, List.of());
            })
            .collect(Collectors.toList());
    }
}
