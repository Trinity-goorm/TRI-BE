package com.trinity.ctc.restaurant.service;

import com.trinity.ctc.category.repository.CategoryRepository;
import com.trinity.ctc.domain.category.entity.Category;
import com.trinity.ctc.domain.restaurant.entity.Restaurant;
import com.trinity.ctc.menu.repository.MenuRepository;
import com.trinity.ctc.restaurant.dto.RestaurantDetailDto;
import com.trinity.ctc.restaurant.repository.RestaurantRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RestaurantService {

    private final RestaurantFileLoader fileLoader;
    private final RestaurantRepository restaurantRepository;
    private final CategoryRepository categoryRepository;
    private final MenuRepository menuRepository;


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
}