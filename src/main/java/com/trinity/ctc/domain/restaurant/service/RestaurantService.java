package com.trinity.ctc.domain.restaurant.service;

import com.trinity.ctc.domain.category.entity.Category;
import com.trinity.ctc.domain.category.repository.CategoryRepository;
import com.trinity.ctc.domain.like.service.LikeService;
import com.trinity.ctc.domain.reservation.dto.ReservationAvailabilityResponse;
import com.trinity.ctc.domain.restaurant.dto.RestaurantDetailResponse;
import com.trinity.ctc.domain.restaurant.dto.RestaurantPreviewRequest;
import com.trinity.ctc.domain.restaurant.dto.RestaurantPreviewResponse;
import com.trinity.ctc.domain.restaurant.entity.Restaurant;
import com.trinity.ctc.domain.restaurant.entity.RestaurantCategory;
import com.trinity.ctc.domain.restaurant.repository.RestaurantCategoryRepository;
import com.trinity.ctc.domain.restaurant.repository.RestaurantRepository;
import com.trinity.ctc.domain.search.sorting.SortingStrategy;
import com.trinity.ctc.domain.search.sorting.SortingStrategyFactory;
import com.trinity.ctc.domain.seat.dto.AvailableSeatPerDay;
import com.trinity.ctc.domain.seat.service.SeatService;
import com.trinity.ctc.domain.user.entity.User;
import com.trinity.ctc.domain.user.repository.UserRepository;
import com.trinity.ctc.global.exception.CustomException;
import com.trinity.ctc.global.exception.error_code.RestaurantErrorCode;
import com.trinity.ctc.global.exception.error_code.UserErrorCode;
import com.trinity.ctc.global.util.validator.SeatAvailabilityValidator;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
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
    private final SeatService seatService;
    private final LikeService likeService;
    private final RestaurantCategoryRepository restaurantCategoryRepository;

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
        List<Restaurant> restaurantList = restaurants.getContent();
        return convertTorestaurantDtoList(restaurantList, user);
    }

    public List<RestaurantPreviewResponse> convertTorestaurantDtoList(List<Restaurant> restaurantList, User user) {
        List<Long> restaurantIds = restaurantList.stream().map(Restaurant::getId).collect(Collectors.toList());
        Map<Long, Boolean> wishMap = likeService.existsByUserAndRestaurantIds(user, restaurantIds);
        Map<Long, List<AvailableSeatPerDay>> rawSeatMap = seatService.findAvailableSeatsGrouped(restaurantIds, LocalDate.now(), LocalDate.now().plusDays(14));

        Map<Long, List<ReservationAvailabilityResponse>> reservationMap = rawSeatMap.entrySet().stream()
            .collect(Collectors.toMap(
                Entry::getKey,
                entry -> processAvailabilityPerRestaurant(entry.getValue())
            ));

        List<RestaurantCategory> rcList = restaurantCategoryRepository.findAllWithCategoryByRestaurantIds(restaurantIds);

        return restaurantList.stream()
            .map(restaurant -> {
                Long restaurantId = restaurant.getId();
                boolean isWishlisted = wishMap.getOrDefault(restaurantId, false);
                List<ReservationAvailabilityResponse> reservation = reservationMap.getOrDefault(restaurantId, Collections.emptyList());

                return RestaurantPreviewResponse.fromEntity(user, restaurant, isWishlisted, reservation, rcList);
            })
            .collect(Collectors.toList());
    }

    private List<ReservationAvailabilityResponse> processAvailabilityPerRestaurant(List<AvailableSeatPerDay> seats) {
        Map<LocalDate, List<AvailableSeatPerDay>> byDate = seats.stream()
            .collect(Collectors.groupingBy(AvailableSeatPerDay::getReservationDate));

        return IntStream.range(0, 14)
            .mapToObj(i -> {
                LocalDate date = LocalDate.now().plusDays(i);
                List<AvailableSeatPerDay> seatList = byDate.getOrDefault(date, Collections.emptyList());
                boolean isAvailable = SeatAvailabilityValidator.isAnySeatAvailableForSearch(seatList, isToday(date));
                return new ReservationAvailabilityResponse(date, isAvailable, null);
            })
            .collect(Collectors.toList());
    }

    private boolean isToday(LocalDate date) {
        return LocalDate.now().equals(date);
    }
}