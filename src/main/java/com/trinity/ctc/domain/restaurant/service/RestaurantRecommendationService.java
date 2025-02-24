package com.trinity.ctc.domain.restaurant.service;

import com.trinity.ctc.domain.ai.AIRecommendationClient;
import com.trinity.ctc.domain.ai.AIRecommendationClientDumy;
import com.trinity.ctc.domain.ai.dto.AIRecommendationRequest;
import com.trinity.ctc.domain.ai.dto.AIRecommendationResponse;
import com.trinity.ctc.domain.like.repository.LikeRepository;
import com.trinity.ctc.domain.restaurant.dto.RestaurantPreviewResponse;
import com.trinity.ctc.domain.restaurant.entity.Restaurant;
import com.trinity.ctc.domain.restaurant.repository.RestaurantRepository;
import com.trinity.ctc.domain.user.entity.User;
import com.trinity.ctc.domain.user.entity.UserPreference;
import com.trinity.ctc.domain.user.repository.UserPreferenceRepository;
import com.trinity.ctc.domain.user.repository.UserRepository;
import com.trinity.ctc.util.exception.CustomException;
import com.trinity.ctc.util.exception.error_code.RestaurantErrorCode;
import com.trinity.ctc.util.exception.error_code.UserErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RestaurantRecommendationService {

    private final AIRecommendationClient aiRecommendationClient;
    private final AIRecommendationClientDumy aiRecommendationClientDumy;
    private final RestaurantRepository restaurantRepository;
    private final LikeRepository likeRepository;
    private final UserRepository userRepository;
    private final UserPreferenceRepository userPreferenceRepository;

    @Transactional
    public List<RestaurantPreviewResponse> getRecommendedRestaurants(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new CustomException(UserErrorCode.NOT_FOUND));

        log.info("user 가져오기: {}", userId);

                                                                // userPreference의 ID는 userID -> 따라서, findById의 parameter는 userID가 됨
        UserPreference userPreference = userPreferenceRepository.findById(userId).orElseThrow(() -> new CustomException(UserErrorCode.NOT_FOUND));

        log.info("userPreference 가져오기: {}", userPreference);
        List<String> preferredCategories = userPreference.getUserPreferenceCategoryList().stream()
            .map(upc -> upc.getCategory().getName())
            .collect(Collectors.toList());

        log.info("preferredCategories 가져오기: {}", preferredCategories);
        AIRecommendationRequest request = new AIRecommendationRequest(user.getId(), preferredCategories);
        log.info("AI 추천 요청: {}", request.getUserId(), request.getPreferredCategories());
//        AIRecommendationResponse aiResponse = aiRecommendationClient.getRecommendations(request);
        AIRecommendationResponse aiResponse = aiRecommendationClientDumy.getRecommendations(request);

        log.info("AI 추천 결과: {}", aiResponse.getUserId(),aiResponse.getRecommendations());

        return aiResponse.getRecommendations().stream()
            .map(recommendation -> {
                Restaurant restaurant = restaurantRepository.findById(recommendation.getRestaurantId())
                    .orElseThrow(() -> new CustomException(RestaurantErrorCode.NOT_FOUND));

                boolean isWishlisted = likeRepository.existsByUserAndRestaurant(user, restaurant);

                return RestaurantPreviewResponse.fromEntity(restaurant, isWishlisted, List.of());
            })
            .collect(Collectors.toList());
    }
}
