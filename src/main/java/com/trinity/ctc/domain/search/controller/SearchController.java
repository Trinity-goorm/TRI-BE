package com.trinity.ctc.domain.search.controller;

import com.trinity.ctc.domain.restaurant.dto.RestaurantPreviewRequest;
import com.trinity.ctc.domain.restaurant.dto.RestaurantPreviewResponse;
import com.trinity.ctc.domain.search.dto.SearchHistoryResponse;
import com.trinity.ctc.domain.search.service.SearchService;
import com.trinity.ctc.domain.user.dto.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/search")
@Tag(name = "Search", description = "검색 관련 API")
public class SearchController {

    private final SearchService searchService;


    @PostMapping("/noauthenticaiton")
    public ResponseEntity<List<RestaurantPreviewResponse>> getRestaurantsBySearchForTest(
        @RequestBody RestaurantPreviewRequest request,
        @RequestParam String keyword) {
        return ResponseEntity.ok(searchService.searchForTest(request, keyword));
    }

    @PostMapping
    @Operation(
            summary = "키워드 검색",
            description = "키워드를 통한 음식점 검색"
    )
    @ApiResponse(
            responseCode = "200",
            description = "검색 성공",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = RestaurantPreviewResponse.class)
            )
    )
    public ResponseEntity<List<RestaurantPreviewResponse>> getRestaurantsBySearch(
        @RequestBody RestaurantPreviewRequest request,
        @RequestParam String keyword,
        @AuthenticationPrincipal CustomUserDetails customUserDetails) {

        String kakaoId = customUserDetails.getUsername();
        return ResponseEntity.ok(searchService.search(kakaoId,request, keyword));
    }

    @GetMapping("/history")
    @Operation(
            summary = "검색 기록 조회",
            description = "사용자의 검색 기록 조회"
    )
    @ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = SearchHistoryResponse.class)
            )
    )
    public ResponseEntity<List<SearchHistoryResponse>> getSearchHistory(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
        String kakaoId = customUserDetails.getUsername();
        return ResponseEntity.ok(searchService.getSearchHistory(kakaoId));
    }

    @DeleteMapping("/history/{searchHistoryId}")
    @Operation(
            summary = "검색 기록 삭제",
            description = "사용자의 검색 기록 삭제"
    )
    @ApiResponse(
            responseCode = "200",
            description = "삭제 성공",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = String.class)
            )
    )
    public ResponseEntity<String> deleteSearchHistory(@PathVariable Long searchHistoryId, @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        String kakaoId = customUserDetails.getUsername();
        searchService.deleteSearchHistory(kakaoId, searchHistoryId);
        return ResponseEntity.ok("검색 기록이 삭제되었습니다.");
    }

}

