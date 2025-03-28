package com.trinity.ctc.domain.restaurant.repository;

import com.trinity.ctc.domain.restaurant.entity.RestaurantCategory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface RestaurantCategoryRepository extends JpaRepository<RestaurantCategory, Long> {

    @Query("""
    SELECT rc FROM RestaurantCategory rc
    JOIN FETCH rc.category
    WHERE rc.restaurant.id IN :restaurantIds
""")
    List<RestaurantCategory> findAllWithCategoryByRestaurantIds(List<Long> restaurantIds);
}
