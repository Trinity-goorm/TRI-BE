package com.trinity.ctc.domain.restaurant.repository.factory;

import com.trinity.ctc.domain.restaurant.entity.Restaurant;
import com.trinity.ctc.domain.restaurant.repository.JdbcRestaurantRepository;
import com.trinity.ctc.domain.restaurant.repository.JpqlRestaurantRepository;
import com.trinity.ctc.domain.restaurant.repository.NativeQueryRestaurantRepository;
import com.trinity.ctc.domain.restaurant.repository.QueryDslRestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

/**
 * 검색 방법을 결정하는 팩토리 클래스
 */
@Component
@RequiredArgsConstructor
public class RestaurantRepositoryFactory {

    private final JpqlRestaurantRepository jpqlRestaurantRepository;
    private final NativeQueryRestaurantRepository nativeQueryRestaurantRepository;
    private final QueryDslRestaurantRepository queryDslRestaurantRepository;
    private final JdbcRestaurantRepository jdbcRestaurantRepository;

    /**
     * 검색 방법에 따라 적절한 리포지토리를 선택
     */
    public Page<Restaurant> searchRestaurants(SearchType searchType, String keyword, Pageable pageable) {
        return switch (searchType) {
            case JPQL -> jpqlRestaurantRepository.searchRestaurants(keyword, pageable);
            case NATIVE_QUERY -> nativeQueryRestaurantRepository.searchRestaurants(keyword, pageable);
            case QUERY_DSL -> queryDslRestaurantRepository.searchRestaurants(keyword, pageable);
            case JDBC -> jdbcRestaurantRepository.searchRestaurants(keyword, pageable);
        };
    }

    /**
     * 검색 방법을 나타내는 열거형
     */
    public enum SearchType {
        JPQL,
        NATIVE_QUERY,
        QUERY_DSL,
        JDBC
    }
}