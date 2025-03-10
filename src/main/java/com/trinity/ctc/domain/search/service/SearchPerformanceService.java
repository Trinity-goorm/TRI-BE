package com.trinity.ctc.domain.search.service;

import com.trinity.ctc.domain.restaurant.dto.RestaurantPreviewRequest;
import com.trinity.ctc.domain.restaurant.entity.Restaurant;
import com.trinity.ctc.domain.restaurant.repository.factory.RestaurantRepositoryFactory;
import com.trinity.ctc.domain.restaurant.service.RestaurantService;
import com.trinity.ctc.domain.search.dto.SearchPerformanceResult;
import com.trinity.ctc.domain.search.dto.SearchPerformanceSingleResult;
import com.trinity.ctc.domain.search.sorting.SortingStrategy;
import com.trinity.ctc.domain.search.sorting.SortingStrategyFactory;
import com.trinity.ctc.domain.user.entity.User;
import com.trinity.ctc.domain.user.repository.UserRepository;
import com.trinity.ctc.global.exception.CustomException;
import com.trinity.ctc.global.exception.error_code.UserErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 검색 성능 측정을 위한 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SearchPerformanceService {

    private final RestaurantRepositoryFactory repositoryFactory;
    private final RestaurantService restaurantService;
    private final UserRepository userRepository;
    private final SearchService searchService;

    /**
     * 모든 검색 방법의 성능을 비교하는 메서드
     */
    @Transactional
    public SearchPerformanceResult compareSearchPerformance(String kakaoId, RestaurantPreviewRequest request, String keyword) {
        // 사용자 정보 조회
        Optional<User> userOptional = userRepository.findByKakaoId(Long.valueOf(kakaoId));
        Long userId = userOptional.orElseThrow(() -> new CustomException(UserErrorCode.NOT_FOUND)).getId();
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new CustomException(UserErrorCode.NOT_FOUND));

        // 검색 설정
        SortingStrategy sortingStrategy = SortingStrategyFactory.getStrategy(request.getSortType());
        Sort sort = sortingStrategy.getSort();
        Pageable pageable = PageRequest.of(request.getPage() - 1, 30, sort);

        List<SearchPerformanceSingleResult> performanceResults = new ArrayList<>();

        log.info("각각 측정합니다:");
        // 각 검색 방법별 성능 측정
        for (RestaurantRepositoryFactory.SearchType searchType : RestaurantRepositoryFactory.SearchType.values()) {
            SearchPerformanceSingleResult result = measureSearchPerformance(
                searchType, keyword, pageable, user);
            performanceResults.add(result);
        }

        // 검색 이력 저장
        searchService.saveSearchHistory(userId, keyword);

        return new SearchPerformanceResult(performanceResults, keyword);
    }

    /**
     * 특정 검색 방법의 성능을 측정하는 메서드
     */
    private SearchPerformanceSingleResult measureSearchPerformance(
        RestaurantRepositoryFactory.SearchType searchType,
        String keyword,
        Pageable pageable,
        User user) {

        log.info("검색 성능 측정 시작: {}", searchType);
        Instant start = Instant.now();

        // 검색 실행
        Page<Restaurant> results = repositoryFactory.searchRestaurants(searchType, keyword, pageable);

        Instant end = Instant.now();
        long executionTime = Duration.between(start, end).toMillis();

        log.info("검색 성능 측정 완료: {}, 실행 시간: {}ms, 결과 수: {}",
            searchType, executionTime, results.getTotalElements());

        // 결과 변환 및 DTO 생성
        return new SearchPerformanceSingleResult(
            searchType.name(),
            executionTime,
            results.getTotalElements(),
            restaurantService.convertToRestaurantDtoList(results, user)
        );
    }
}