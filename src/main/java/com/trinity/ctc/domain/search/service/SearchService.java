package com.trinity.ctc.domain.search.service;

import com.trinity.ctc.domain.restaurant.dto.RestaurantPreviewRequest;
import com.trinity.ctc.domain.restaurant.dto.RestaurantPreviewResponse;
import com.trinity.ctc.domain.restaurant.entity.Restaurant;
import com.trinity.ctc.domain.restaurant.repository.RestaurantRepository;
import com.trinity.ctc.domain.restaurant.service.RestaurantService;
import com.trinity.ctc.domain.search.dto.SearchHistoryResponse;
import com.trinity.ctc.domain.search.entity.SearchHistory;
import com.trinity.ctc.domain.search.repository.SearchRepository;
import com.trinity.ctc.domain.search.sorting.SortingStrategy;
import com.trinity.ctc.domain.search.sorting.SortingStrategyFactory;
import com.trinity.ctc.domain.user.entity.User;
import com.trinity.ctc.domain.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SearchService {
    private final RestaurantRepository restaurantRepository;
    private final RestaurantService restaurantService;
    private final UserRepository userRepository;
    private final SearchRepository searchRepository;

    @Transactional()
    public List<RestaurantPreviewResponse> search(RestaurantPreviewRequest request, String keyword) {
        SortingStrategy sortingStrategy = SortingStrategyFactory.getStrategy(request.getSortType());
        Sort sort = sortingStrategy.getSort();

        Pageable pageable = PageRequest.of(request.getPage()-1, 30, sort);

        Page<Restaurant> restaurants = restaurantRepository.searchRestaurants(keyword, pageable);
        User user = userRepository.findById(request.getUserId())
            .orElseThrow(() -> new IllegalArgumentException("해당 유저를 찾을 수 없습니다. ID: " + request.getUserId()));

        saveSearchHistory(request.getUserId(), keyword);
        return restaurantService.convertToRestaurantDtoList(restaurants,user);
    }

    public List<SearchHistoryResponse> getSearchHistory(Long userId) {
        return searchRepository.findTopByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(0, 15));
    }

    @Transactional
    public void saveSearchHistory(Long userId, String keyword) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("해당 유저를 찾을 수 없습니다. ID: " + userId));

        Optional<SearchHistory> existingHistory = searchRepository.findByKeywordAndUser(keyword, user);

        if(existingHistory.isPresent()) {
            SearchHistory searchHistory = existingHistory.get();
            searchHistory.updateCreatedAt();
            searchRepository.save(searchHistory);
        } else {
            SearchHistory newSearchHistory = SearchHistory.builder()
                .keyword(keyword)
                .createdAt(LocalDateTime.now())
                .user(user)
                .build();
            searchRepository.save(newSearchHistory);
        }
    }

    @Transactional
    public void deleteSearchHistory(Long userId, Long searchHistoryId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("해당 유저를 찾을 수 없습니다. ID: " + userId));

        SearchHistory searchHistory = searchRepository.findById(searchHistoryId)
            .orElseThrow(
                () -> new IllegalArgumentException("검색 기록을 찾을 수 없습니다. ID: " + searchHistoryId));

        if (searchHistory.getUser().equals(user)) {
            searchHistory.softDelete(); // 🔹 isDeleted = true로 변경
            searchRepository.save(searchHistory);
        }
    }
}
