package com.trinity.ctc.domain.restaurant.controller;

import com.trinity.ctc.domain.restaurant.dto.RestaurantPreviewResponse;
import com.trinity.ctc.domain.restaurant.dto.RestaurantDetailResponse;
import com.trinity.ctc.domain.restaurant.dto.RestaurantPreviewRequest;
import com.trinity.ctc.domain.restaurant.service.RestaurantService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/restaurants")
@RequiredArgsConstructor
public class RestaurantController {

    private final RestaurantService restaurantService;

    //식당 상세정보 반환
    @GetMapping("/{restaurantId}")
    public ResponseEntity<RestaurantDetailResponse> getRestaurantDetail(
        @PathVariable Long restaurantId) {
        return ResponseEntity.ok(restaurantService.getRestaurantDetail(restaurantId));
    }

    //카테고리별 식당 목록 반환
    @PostMapping("/category/{categoryId}")
    public ResponseEntity<List<RestaurantPreviewResponse>> getRestaurantsByCategory(
        @RequestBody RestaurantPreviewRequest request, @PathVariable Long categoryId) {
        return ResponseEntity.ok(restaurantService.getRestaurantsByCategory(request, categoryId));
    }
}
