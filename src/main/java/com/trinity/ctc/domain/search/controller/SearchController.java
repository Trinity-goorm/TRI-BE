package com.trinity.ctc.domain.search.controller;

import com.trinity.ctc.domain.restaurant.dto.RestaurantPreviewRequest;
import com.trinity.ctc.domain.restaurant.dto.RestaurantPreviewResponse;
import com.trinity.ctc.domain.search.service.SearchService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
public class SearchController {

    private final SearchService searchService;

    //검색어로 식당 검색
    @PostMapping()
    public ResponseEntity<List<RestaurantPreviewResponse>> getRestaurantsBySearch(@RequestBody
    RestaurantPreviewRequest request, @RequestParam String keyword) {
        return ResponseEntity.ok(searchService.search(request, keyword));
    }

    @GetMapping("/history/{userId}")
    public ResponseEntity<List<String>> getSearchHistory(@PathVariable Long userId) {
        return ResponseEntity.ok(searchService.getSearchHistory(userId));
    }
}

