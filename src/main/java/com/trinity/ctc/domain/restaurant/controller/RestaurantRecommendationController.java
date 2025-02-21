package com.trinity.ctc.domain.restaurant.controller;

import com.trinity.ctc.domain.restaurant.dto.RestaurantPreviewResponse;
import com.trinity.ctc.domain.restaurant.service.RestaurantRecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/restaurants/recommendation")
@RequiredArgsConstructor
public class RestaurantRecommendationController {

    private final RestaurantRecommendationService restaurantRecommendationService;

    @GetMapping("/{userId}")
    public ResponseEntity<List<RestaurantPreviewResponse>> getRecommendedRestaurants(@PathVariable Long userId) {
        return ResponseEntity.ok(restaurantRecommendationService.getRecommendedRestaurants(userId));
    }
}
