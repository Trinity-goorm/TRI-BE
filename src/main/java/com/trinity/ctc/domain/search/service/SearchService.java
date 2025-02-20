package com.trinity.ctc.domain.search.service;

import com.trinity.ctc.domain.restaurant.dto.RestaurantPreviewResponse;
import com.trinity.ctc.domain.restaurant.entity.Restaurant;
import com.trinity.ctc.domain.restaurant.repository.RestaurantRepository;
import com.trinity.ctc.domain.restaurant.service.RestaurantService;
import com.trinity.ctc.domain.restaurant.dto.RestaurantPreviewRequest;
import com.trinity.ctc.domain.search.sorting.SortingStrategy;
import com.trinity.ctc.domain.search.sorting.SortingStrategyFactory;
import com.trinity.ctc.domain.user.entity.User;
import com.trinity.ctc.kakao.repository.UserRepository;
import java.util.List;
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

    @Transactional(readOnly = true)
    public List<RestaurantPreviewResponse> search(RestaurantPreviewRequest request, String keyword) {
        SortingStrategy sortingStrategy = SortingStrategyFactory.getStrategy(request.getSortType());
        Sort sort = sortingStrategy.getSort(); //getSort() 메서드가 없음

        Pageable pageable = PageRequest.of(request.getPage()-1, 30, sort);

        Page<Restaurant> restaurants = restaurantRepository.searchRestaurants(keyword, pageable);
        User user = userRepository.findById(request.getUserId())
            .orElseThrow(() -> new IllegalArgumentException("해당 유저를 찾을 수 없습니다. ID: " + request.getUserId()));

        return restaurantService.convertToRestaurantDtoList(restaurants,user);
    }
}
