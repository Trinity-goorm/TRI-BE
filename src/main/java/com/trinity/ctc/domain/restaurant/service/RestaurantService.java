package com.trinity.ctc.domain.restaurant.service;

import com.trinity.ctc.domain.category.repository.CategoryRepository;
import com.trinity.ctc.domain.category.entity.Category;
import com.trinity.ctc.domain.reservation.dto.ReservationAvailabilityDto;
import com.trinity.ctc.domain.restaurant.entity.Restaurant;
import com.trinity.ctc.domain.seat.service.SeatAvailabilityService;
import com.trinity.ctc.domain.user.entity.User;
import com.trinity.ctc.kakao.repository.UserRepository;
import com.trinity.ctc.domain.like.repository.LikeRepository;
import com.trinity.ctc.domain.restaurant.dto.RestaurantListDto;
import com.trinity.ctc.domain.restaurant.dto.RestaurantDetailDto;
import com.trinity.ctc.domain.restaurant.repository.RestaurantRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    public void insertRestaurantsFromFile() {
        List<Category> categories = categoryRepository.findAll();
        List<Restaurant> restaurants = fileLoader.loadRestaurantsFromFile(categories);
        restaurantRepository.saveAll(restaurants);
    }
    @Transactional(readOnly = true)
    public RestaurantDetailDto getRestaurantDetail(Long restaurantId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
            .orElseThrow(
                () -> new IllegalArgumentException("해당 식당을 찾을 수 없습니다. ID: " + restaurantId));

        return RestaurantDetailDto.fromEntity(restaurant);
    }

    @Transactional(readOnly = true)
    public List<RestaurantListDto> getRestaurantsByCategory(Long categoryId, Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("해당 유저를 찾을 수 없습니다. ID: " + userId));

        List<Restaurant> restaurants = restaurantRepository.findByCategory(categoryId);
        log.info("restaurants: {}", restaurants.size());

        return convertToRestaurantDtoList(restaurants, user);
    }

    public List<RestaurantListDto> convertToRestaurantDtoList(List<Restaurant> restaurants, User user) {
        return restaurants.stream()
            .map(restaurant -> {
                boolean isWishlisted = likeRepository.existsByUserAndRestaurant(user, restaurant);

                // 14일간 날짜별 예약 가능 여부 조회
                List<ReservationAvailabilityDto> reservation = seatAvailabilityService
                    .getAvailabilityForNext14Days(restaurant.getId());

                log.info("reservation 사이즈: {}", reservation.size());
                return RestaurantListDto.fromEntity(restaurant, isWishlisted, reservation);
            })
            .collect(Collectors.toList());
    }

}