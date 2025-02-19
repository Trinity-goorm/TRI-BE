package com.trinity.ctc.domain.restaurant.service;

import com.trinity.ctc.category.repository.CategoryRepository;
import com.trinity.ctc.domain.category.entity.Category;
import com.trinity.ctc.domain.restaurant.entity.Restaurant;
import com.trinity.ctc.domain.user.entity.User;
import com.trinity.ctc.kakao.repository.UserRepository;
import com.trinity.ctc.domain.like.repository.LikeRepository;
import com.trinity.ctc.domain.restaurant.dto.RestaurantCategoryListDto;
import com.trinity.ctc.domain.restaurant.dto.RestaurantDetailDto;
import com.trinity.ctc.domain.restaurant.repository.RestaurantRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RestaurantService {

    private final RestaurantFileLoader fileLoader;
    private final RestaurantRepository restaurantRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final LikeRepository likeRepository;

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
    public List<RestaurantCategoryListDto> getRestaurantsByCategory(Long categoryId, Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("해당 유저를 찾을 수 없습니다. ID: " + userId));

        List<Restaurant> restaurants = restaurantRepository.findByCategory(categoryId);

        return restaurants.stream()
            .map(restaurant -> RestaurantCategoryListDto.fromEntity(
                restaurant,
                likeRepository.existsByUserAndRestaurant(user, restaurant))) // 사용자 찜 여부 확인
            .collect(Collectors.toList());
    }
}