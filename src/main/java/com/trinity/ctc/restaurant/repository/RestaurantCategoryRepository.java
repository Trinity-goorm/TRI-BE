package com.trinity.ctc.restaurant.repository;

import com.trinity.ctc.domain.restaurant.entity.RestaurantCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RestaurantCategoryRepository extends JpaRepository<RestaurantCategory, Long> {

}
