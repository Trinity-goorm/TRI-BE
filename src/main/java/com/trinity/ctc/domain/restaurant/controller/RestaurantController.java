package com.trinity.ctc.domain.restaurant.controller;

import com.trinity.ctc.domain.restaurant.dto.RestaurantPreviewResponse;
import com.trinity.ctc.domain.restaurant.dto.RestaurantDetailResponse;
import com.trinity.ctc.domain.restaurant.dto.RestaurantPreviewRequest;
import com.trinity.ctc.domain.restaurant.service.RestaurantService;
import com.trinity.ctc.domain.user.dto.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/restaurants")
@RequiredArgsConstructor
@Tag(name = "Restaurant", description = "식당 관련 API")
public class RestaurantController {

    private final RestaurantService restaurantService;

    //식당 상세정보 반환
    @GetMapping("/{restaurantId}")
    @Operation(
            summary = "식당 상세정보",
            description = "식당의 상세정보를 반환하는 기능"
    )
    @ApiResponse(
            responseCode = "200",
            description = "식당 상세정보 반환 성공",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = RestaurantDetailResponse.class)
            )
    )
    public ResponseEntity<RestaurantDetailResponse> getRestaurantDetail(
            @PathVariable Long restaurantId) {
        return ResponseEntity.ok(restaurantService.getRestaurantDetail(restaurantId));
    }

    //카테고리별 식당 목록 반환
    @PostMapping("/category/{categoryId}")
    @Operation(
            summary = "카테고리별 식당 목록",
            description = "카테고리별 식당 목록을 반환하는 기능"
    )
    @ApiResponse(
            responseCode = "200",
            description = "카테고리별 식당 목록 반환 성공",
            content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = RestaurantPreviewResponse.class))
            )
    )
    public ResponseEntity<List<RestaurantPreviewResponse>> getRestaurantsByCategory(
            @RequestBody RestaurantPreviewRequest request,
            @PathVariable Long categoryId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        String kakaoId = userDetails.getUsername();
        return ResponseEntity.ok(restaurantService.getRestaurantsByCategory(kakaoId, request, categoryId));
    }
}
