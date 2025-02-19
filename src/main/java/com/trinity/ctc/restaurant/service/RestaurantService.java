package com.trinity.ctc.restaurant.service;

import com.trinity.ctc.category.repository.CategoryRepository;
import com.trinity.ctc.domain.category.entity.Category;
import com.trinity.ctc.domain.restaurant.entity.Restaurant;
import com.trinity.ctc.menu.repository.MenuRepository;
import com.trinity.ctc.restaurant.repository.RestaurantRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class RestaurantService {

    private final RestaurantFileLoader fileLoader;
    private final RestaurantRepository restaurantRepository;
    private final CategoryRepository categoryRepository;
    private final MenuRepository menuRepository;

    public RestaurantService(RestaurantFileLoader fileLoader, RestaurantRepository restaurantRepository, CategoryRepository categoryRepository, MenuRepository menuRepository) {
        this.fileLoader = fileLoader;
        this.restaurantRepository = restaurantRepository;
        this.categoryRepository = categoryRepository;
        this.menuRepository = menuRepository;
    }

    public void insertRestaurantsFromFile() {
        List<Category> categories = categoryRepository.findAll();
        List<Restaurant> restaurants = fileLoader.loadRestaurantsFromFile(categories);
        restaurantRepository.saveAll(restaurants);
    }
}