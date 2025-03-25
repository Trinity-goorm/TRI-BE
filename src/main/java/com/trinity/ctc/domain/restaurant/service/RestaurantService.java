package com.trinity.ctc.domain.restaurant.service;

import com.trinity.ctc.domain.category.entity.Category;
import com.trinity.ctc.domain.category.repository.CategoryRepository;
import com.trinity.ctc.domain.like.repository.LikeRepository;
import com.trinity.ctc.domain.reservation.dto.ReservationAvailabilityResponse;
import com.trinity.ctc.domain.restaurant.dto.RestaurantDetailResponse;
import com.trinity.ctc.domain.restaurant.dto.RestaurantPreviewRequest;
import com.trinity.ctc.domain.restaurant.dto.RestaurantPreviewResponse;
import com.trinity.ctc.domain.restaurant.entity.Restaurant;
import com.trinity.ctc.domain.restaurant.repository.RestaurantRepository;
import com.trinity.ctc.domain.search.sorting.SortingStrategy;
import com.trinity.ctc.domain.search.sorting.SortingStrategyFactory;
import com.trinity.ctc.domain.seat.service.SeatService;
import com.trinity.ctc.domain.user.dto.CustomUserDetails;
import com.trinity.ctc.domain.user.entity.User;
import com.trinity.ctc.domain.user.repository.UserRepository;
import com.trinity.ctc.global.exception.CustomException;
import com.trinity.ctc.global.exception.error_code.RestaurantErrorCode;
import com.trinity.ctc.global.exception.error_code.UserErrorCode;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final SeatService seatService;

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
                        () -> new CustomException(RestaurantErrorCode.NOT_FOUND));

        log.info("[SELECT] 식당 상세정보 획득 ID: {}", restaurantId);
        return RestaurantDetailResponse.fromEntity(restaurant);
    }

    @Transactional(readOnly = true)
    public List<RestaurantPreviewResponse> getRestaurantsByCategory(String kakaoId, RestaurantPreviewRequest request, Long categoryId) {
        Optional<User> userOptional = userRepository.findByKakaoId(Long.valueOf(kakaoId));
        if (userOptional.isEmpty()) {
            throw new CustomException(UserErrorCode.NOT_FOUND);
        }
        User user = userOptional.get();

        SortingStrategy sortingStrategy = SortingStrategyFactory.getStrategy(request.getSortType());
        Sort sort = sortingStrategy.getSort();
        Pageable pageable = PageRequest.of(request.getPage() - 1, 30, sort);

        Page<Restaurant> restaurants = restaurantRepository.findByCategory(categoryId, pageable);

        return convertToRestaurantDtoList(restaurants, user);
    }

    public List<RestaurantPreviewResponse> convertToRestaurantDtoList(Page<Restaurant> restaurants, User user) {
        List<RestaurantPreviewResponse> result = restaurants.stream()
            .map(restaurant -> {
                boolean isWishlisted = likeRepository.existsByUserAndRestaurant(user, restaurant);

                // 14일간 날짜별 예약 가능 여부 조회
                List<ReservationAvailabilityResponse> reservation = seatService
                    .getAvailabilityForNext14Days(restaurant.getId());

                log.info("reservation 사이즈: {}", reservation.size());
                return RestaurantPreviewResponse.fromEntity(user,restaurant, isWishlisted, reservation);
            })
            .collect(Collectors.toList());
        return result;
    }
}