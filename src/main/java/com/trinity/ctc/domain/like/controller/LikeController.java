package com.trinity.ctc.domain.like.controller;

import com.trinity.ctc.domain.like.dto.LikeResponse;
import com.trinity.ctc.domain.like.dto.UnLikeResponse;
import com.trinity.ctc.domain.like.service.LikeService;
import com.trinity.ctc.domain.restaurant.dto.RestaurantDetailResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/restaurants/like")
@RequiredArgsConstructor
@Tag(name = "Like", description = "찜하기 관련 API")
public class LikeController {

    private final LikeService likeService;

    @PostMapping("/{userId}/{restaurantId}")
    @Operation(
        summary = "찜하기",
        description = "사용자가 식당을 찜하는 기능"
    )
    @ApiResponse(
        responseCode = "200",
        description = "찜하기 성공",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = LikeResponse.class)
        )
    )
    public ResponseEntity<LikeResponse> likeRestaurant(@PathVariable Long userId,
        @PathVariable Long restaurantId) {
        try {
            likeService.likeRestaurant(userId, restaurantId);
            return ResponseEntity.ok(new LikeResponse(true, "찜하기 완료"));
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new LikeResponse(false, e.getMessage()));
        }
    }

    @DeleteMapping("/{userId}/{restaurantId}")
    @Operation(
        summary = "찜하기 취소",
        description = "사용자가 찜한 식당을 찜하기 취소하는 기능"
    )
    @ApiResponse(
        responseCode = "200",
        description = "찜하기 취소 성공",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = UnLikeResponse.class)
        )
    )
    public ResponseEntity<UnLikeResponse> unlikeRestaurant(@PathVariable Long userId,
        @PathVariable Long restaurantId) {
        try {
            likeService.unlikeRestaurant(userId, restaurantId);
            return ResponseEntity.ok(new UnLikeResponse(true, "찜하기 취소 완료"));

        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new UnLikeResponse(false, e.getMessage()));
        }
    }

    @GetMapping("{userId}")
    @Operation(
        summary = "찜한 식당 목록 조회",
        description = "사용자가 찜한 식당 목록을 조회하는 기능"
    )
    @ApiResponse(
        responseCode = "200",
        description = "조회 성공",
        content = @Content(
            mediaType = "application/json",
            array = @ArraySchema(schema = @Schema(implementation = RestaurantDetailResponse.class))
        )
    )
    public ResponseEntity<List<RestaurantDetailResponse>> getLikeList(@PathVariable Long userId) {
        return ResponseEntity.ok(likeService.getLikeList(userId));
    }
}
