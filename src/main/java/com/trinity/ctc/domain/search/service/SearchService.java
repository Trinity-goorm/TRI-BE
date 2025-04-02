package com.trinity.ctc.domain.search.service;

import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;

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
import com.trinity.ctc.global.exception.CustomException;
import com.trinity.ctc.global.exception.error_code.SearchErrorCode;
import com.trinity.ctc.global.exception.error_code.UserErrorCode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchService {
    private final RestaurantRepository restaurantRepository;
    private final RestaurantService restaurantService;
    private final UserRepository userRepository;
    private final SearchRepository searchRepository;

    @Transactional(propagation = REQUIRES_NEW)
    public List<RestaurantPreviewResponse> searchByJPQL(RestaurantPreviewRequest request, String keyword) {
        Sort sort = SortingStrategyFactory.getStrategy(request.getSortType()).getSort();
        Pageable pageable = PageRequest.of(request.getPage() - 1, 30, sort);
        List<Long> ids = restaurantRepository.searchRestaurantIdsJPQL(keyword, pageable).getContent();
        return executeSearch(r -> ids, request, keyword);
    }

    @Transactional(propagation = REQUIRES_NEW)
    public List<RestaurantPreviewResponse> searchByNative(RestaurantPreviewRequest request, String keyword) {
        int page = request.getPage() - 1;
        int limit = 30;
        int offset = page * limit;
        return executeSearch(r -> restaurantRepository.searchRestaurantIdsNative(keyword, limit, offset), request, keyword);
    }

//    public List<RestaurantPreviewResponse> searchByQuerydsl(RestaurantPreviewRequest request, String keyword) {
//        int page = request.getPage() - 1;
//        int limit = 30;
//        int offset = page * limit;
//        return executeSearch(r -> restaurantRepository.searchRestaurantIdsQuerydsl(keyword, offset, limit), request, keyword);
//    }



    private List<RestaurantPreviewResponse> executeSearch(
        Function<RestaurantPreviewRequest, List<Long>> searchFunction,
        RestaurantPreviewRequest request,
        String keyword
    ) {
        List<Long> ids = searchFunction.apply(request);
        List<Restaurant> restaurants = restaurantRepository.findAllByIdIn(ids);
        User user = userRepository.findById(1L).orElseThrow(() -> new CustomException(UserErrorCode.NOT_FOUND));
        saveSearchHistory(1L, keyword);
        return restaurantService.convertTorestaurantDtoList(restaurants, user);
    }

    @Transactional
    public List<RestaurantPreviewResponse> search(String kakaoId, RestaurantPreviewRequest request, String keyword) {
        Optional<User> userOptional = userRepository.findByKakaoId(Long.valueOf(kakaoId));
        Long userId = userOptional.orElseThrow(() -> new CustomException(UserErrorCode.NOT_FOUND)).getId();
        log.info("식당 검색 시 userId: {}", userId);

        SortingStrategy sortingStrategy = SortingStrategyFactory.getStrategy(request.getSortType());
        Sort sort = sortingStrategy.getSort();

        Pageable pageable = PageRequest.of(request.getPage() - 1, 30, sort);

        Slice<Long> idPage = restaurantRepository.searchRestaurantIdsJPQL(keyword, pageable);
        List<Restaurant> restaurants = restaurantRepository.findAllByIdIn(idPage.getContent());
//        List<Restaurant> restaurantList = restaurants.getContent();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.NOT_FOUND));

        saveSearchHistory(userId, keyword);

        return restaurantService.convertTorestaurantDtoList(restaurants, user);
    }

    public List<SearchHistoryResponse> getSearchHistory(String kakaoId) {
        Optional<User> userOptional = userRepository.findByKakaoId(Long.valueOf(kakaoId));
        Long userId = userOptional.orElseThrow(() -> new CustomException(UserErrorCode.NOT_FOUND)).getId();

        return searchRepository.findTopByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(0, 15));
    }

    @Transactional
    public void saveSearchHistory(Long userId, String keyword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.NOT_FOUND));

        Optional<SearchHistory> existingHistory = searchRepository.findByKeywordAndUser(keyword, user);

        if (existingHistory.isPresent()) {
            SearchHistory searchHistory = existingHistory.get();
            searchHistory.updateCreatedAt();
            searchHistory.restore();
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
    public void deleteSearchHistory(String kakaoId, Long searchHistoryId) {
        Optional<User> userOptional = userRepository.findByKakaoId(Long.valueOf(kakaoId));
        User user = userOptional.orElseThrow(() -> new CustomException(UserErrorCode.NOT_FOUND));

        SearchHistory searchHistory = searchRepository.findById(searchHistoryId)
                .orElseThrow(
                        () -> new CustomException(SearchErrorCode.NOT_FOUND_SEARCH_RESULT));

        if (searchHistory.getUser().equals(user)) {
            searchHistory.softDelete(); // 🔹 isDeleted = true로 변경
            searchRepository.save(searchHistory);
        }
    }
}
