package com.trinity.ctc.domain.restaurant.repository;

import com.trinity.ctc.domain.restaurant.dto.RestaurantCategoryName;
import com.trinity.ctc.domain.restaurant.entity.RestaurantCategory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface RestaurantCategoryRepository extends JpaRepository<RestaurantCategory, Long> {

    @Query("""
    SELECT new com.trinity.ctc.domain.restaurant.dto.RestaurantCategoryName(rc.restaurant.id,rc.category.name) 
    FROM RestaurantCategory rc
    JOIN rc.category
    WHERE rc.restaurant.id IN :restaurantIds
""")
    List<RestaurantCategoryName> findAllWithCategoryByRestaurantIds(List<Long> restaurantIds);
}
