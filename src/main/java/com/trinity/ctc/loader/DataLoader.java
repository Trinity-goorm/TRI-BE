package com.trinity.ctc.loader;

import com.trinity.ctc.domain.category.service.CategoryService;
import com.trinity.ctc.domain.restaurant.service.RestaurantService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataLoader implements CommandLineRunner {

    private final CategoryService categoryService;
    private final RestaurantService restaurantService;

    public DataLoader(CategoryService categoryService, RestaurantService restaurantService) {
        this.categoryService = categoryService;
        this.restaurantService = restaurantService;
    }

    @Override
    public void run(String... args) {
        categoryService.insertCategoriesFromFile();
        //동일 카테고리
        restaurantService.insertRestaurantsFromFile();
        System.out.println("Data loaded successfully");
    }
}
