package com.trinity.ctc.domain.restaurant.repository;

import com.trinity.ctc.domain.restaurant.entity.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {
}
