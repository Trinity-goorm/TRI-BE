package com.trinity.ctc.restaurant.controller;

import com.trinity.ctc.restaurant.dto.RestaurantCategoryListDto;
import com.trinity.ctc.restaurant.dto.RestaurantDetailDto;
import com.trinity.ctc.restaurant.service.RestaurantService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/restaurants")
@RequiredArgsConstructor
public class RestaurantController {

    private final RestaurantService restaurantService;

    //식당 상세정보 반환
    @GetMapping("/{restaurantId}")
    public ResponseEntity<RestaurantDetailDto> getRestaurantDetail(@PathVariable Long restaurantId) {
        return ResponseEntity.ok(restaurantService.getRestaurantDetail(restaurantId));
    }

    @GetMapping("/category/{categoryId}/{userId}")
    public ResponseEntity<List<RestaurantCategoryListDto>> getRestaurantsByCategory(
        @PathVariable Long categoryId, @PathVariable Long userId) {
        return ResponseEntity.ok(restaurantService.getRestaurantsByCategory(categoryId, userId));
    }
}
