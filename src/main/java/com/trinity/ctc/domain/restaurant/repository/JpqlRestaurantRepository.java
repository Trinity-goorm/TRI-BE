package com.trinity.ctc.domain.restaurant.repository;

import com.trinity.ctc.domain.restaurant.entity.Restaurant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public interface JpqlRestaurantRepository {
    Page<Restaurant> searchRestaurants(String keyword, Pageable pageable);
}