package com.trinity.ctc.domain.restaurant.service;

import com.trinity.ctc.domain.category.repository.CategoryRepository;
import com.trinity.ctc.domain.category.entity.Category;
import com.trinity.ctc.domain.reservation.dto.ReservationAvailabilityResponse;
import com.trinity.ctc.domain.restaurant.dto.RestaurantPreviewRequest;
import com.trinity.ctc.domain.restaurant.entity.Restaurant;
import com.trinity.ctc.domain.search.sorting.SortingStrategy;
import com.trinity.ctc.domain.search.sorting.SortingStrategyFactory;
import com.trinity.ctc.domain.seat.service.SeatAvailabilityService;
import com.trinity.ctc.domain.user.entity.User;
import com.trinity.ctc.kakao.repository.UserRepository;
import com.trinity.ctc.domain.like.repository.LikeRepository;
import com.trinity.ctc.domain.restaurant.dto.RestaurantPreviewResponse;
import com.trinity.ctc.domain.restaurant.dto.RestaurantDetailResponse;
import com.trinity.ctc.domain.restaurant.repository.RestaurantRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RestaurantService {

    private final RestaurantFileLoader fileLoader;
    private final RestaurantRepository restaurantRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final LikeRepository likeRepository;
    private final SeatAvailabilityService seatAvailabilityService;

    @Transactional(readOnly = true)
    public List<Restaurant> getAllRestaurants() {
        log.info("[SELECT] 모든 레스토랑 획득");
        return restaurantRepository.findAll();
    }

    public void insertRestaurantsFromFile() {
        List<Category> categories = categoryRepository.findAll();
        List<Restaurant> restaurants = fileLoader.loadRestaurantsFromFile(categories);
        restaurantRepository.saveAll(restaurants);
    }

    @Transactional(readOnly = true)
    public RestaurantDetailResponse getRestaurantDetail(Long restaurantId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
            .orElseThrow(
                () -> new IllegalArgumentException("해당 식당을 찾을 수 없습니다. ID: " + restaurantId));

        log.info("[SELECT] 식당 상세정보 획득 ID: {}", restaurantId);
        return RestaurantDetailResponse.fromEntity(restaurant);
    }

    @Transactional(readOnly = true)
    public List<RestaurantPreviewResponse> getRestaurantsByCategory(RestaurantPreviewRequest request, Long categoryId) {
        User user = userRepository.findById(request.getUserId())
            .orElseThrow(() -> new IllegalArgumentException("해당 유저를 찾을 수 없습니다. ID: " + request.getUserId()));

        SortingStrategy sortingStrategy = SortingStrategyFactory.getStrategy(request.getSortType());
        Sort sort = sortingStrategy.getSort(); //getSort() 메서드가 없음

        Pageable pageable = PageRequest.of(request.getPage()-1, 30, sort);

        Page<Restaurant> restaurants = restaurantRepository.findByCategory(categoryId, pageable);


        return convertToRestaurantDtoList(restaurants, user);
    }

    public List<RestaurantPreviewResponse> convertToRestaurantDtoList(Page<Restaurant> restaurants, User user) {
        return restaurants.stream()
            .map(restaurant -> {
                boolean isWishlisted = likeRepository.existsByUserAndRestaurant(user, restaurant);

                // 14일간 날짜별 예약 가능 여부 조회
                List<ReservationAvailabilityResponse> reservation = seatAvailabilityService
                    .getAvailabilityForNext14Days(restaurant.getId());

                log.info("reservation 사이즈: {}", reservation.size());
                return RestaurantPreviewResponse.fromEntity(restaurant, isWishlisted, reservation);
            })
            .collect(Collectors.toList());
    }
}