package com.trinity.ctc.domain.like.controller;

import com.trinity.ctc.domain.like.service.LikeService;
import com.trinity.ctc.domain.restaurant.dto.RestaurantDetailDto;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/restaurants/like")
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;

    @PostMapping("/{userId}/{restaurantId}")
    public ResponseEntity<Map<Boolean, String>> likeRestaurant(@PathVariable Long userId,
        @PathVariable Long restaurantId) {
        try {
            likeService.likeRestaurant(userId, restaurantId);
            return ResponseEntity.ok(Map.of(true, "찜하기 완료"));
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(false, e.getMessage()));
        }
    }

    @DeleteMapping("/{userId}/{restaurantId}")
    public ResponseEntity<Map<Boolean, String>> unlikeRestaurant(@PathVariable Long userId,
        @PathVariable Long restaurantId) {
        try {
            likeService.unlikeRestaurant(userId, restaurantId);
            return ResponseEntity.ok(Map.of(true, "찜하기 취소 완료"));

        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(false, e.getMessage()));
        }
    }

    @GetMapping("{userId}")
    public ResponseEntity<List<RestaurantDetailDto>> getLikeList(@PathVariable Long userId) {
        return ResponseEntity.ok(likeService.getLikeList(userId));
    }
}
