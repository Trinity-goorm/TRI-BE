package com.trinity.ctc.domain.restaurant.controller;

import com.trinity.ctc.domain.restaurant.dto.RestaurantPreviewResponse;
import com.trinity.ctc.domain.restaurant.service.RestaurantRecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/restaurants/recommendation")
@RequiredArgsConstructor
@Tag(name = "RestaurantRecommendation", description = "식당 추천 API")
public class RestaurantRecommendationController {

    private final RestaurantRecommendationService restaurantRecommendationService;

    @GetMapping("/{userId}")
    @Operation(
            summary = "사용자에게 추천된 식당 목록 반환",
            description = "사용자에게 추천된 식당 목록을 반환하는 기능"
    )
    @ApiResponse(
            responseCode = "200",
            description = "식당 목록 반환 성공",
            content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = RestaurantPreviewResponse.class)
                    )
            )
    )
    public ResponseEntity<List<RestaurantPreviewResponse>> getRecommendedRestaurants(@PathVariable Long userId) {
        return ResponseEntity.ok(restaurantRecommendationService.getRecommendedRestaurants(userId));
    }
}
