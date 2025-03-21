package com.trinity.ctc.domain.search.controller;

import com.trinity.ctc.domain.restaurant.dto.RestaurantPreviewRequest;
import com.trinity.ctc.domain.search.dto.SearchPerformanceResult;
import com.trinity.ctc.domain.search.service.SearchPerformanceService;
import com.trinity.ctc.domain.user.dto.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 검색 성능 측정 컨트롤러
 */
@RestController
@RequestMapping("/api/v1/search/performance")
@RequiredArgsConstructor
@Tag(name = "Search Performance API", description = "검색 성능 측정 API")
public class SearchPerformanceController {

    private final SearchPerformanceService searchPerformanceService;

    @Operation(summary = "검색 성능 비교 API", description = "다양한 쿼리 방식의 검색 성능을 비교합니다")
    @PostMapping
    public ResponseEntity<SearchPerformanceResult> compareSearchPerformance(
        @RequestBody RestaurantPreviewRequest request,
        @RequestParam String keyword,
        @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        return ResponseEntity.ok(
            searchPerformanceService.compareSearchPerformance(customUserDetails.getUsername(), request, keyword));
    }
}