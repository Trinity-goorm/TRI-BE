package com.trinity.ctc.domain.search.controller;

import com.trinity.ctc.domain.restaurant.dto.RestaurantListResponse;
import com.trinity.ctc.domain.search.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/search")
public class SearchController {

    private final SearchService searchService;

    @GetMapping("{userId}")
    public ResponseEntity<List<RestaurantListResponse>> getRestaurantsBySearch(@RequestParam String keyword, @PathVariable Long userId) {
        return ResponseEntity.ok(searchService.search(keyword, userId));
    }

}

