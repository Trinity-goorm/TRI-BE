package com.trinity.ctc.domain.restaurant.repository;

import com.trinity.ctc.domain.restaurant.entity.Restaurant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

    @Query("SELECT r FROM Restaurant r JOIN r.restaurantCategoryList rc WHERE rc.category.id = :categoryId")
    List<Restaurant> findByCategory(@Param("categoryId") Long categoryId);

    @Query("SELECT DISTINCT r FROM Restaurant r " +
        "LEFT JOIN r.menus m " +
        "LEFT JOIN r.restaurantCategoryList rc " +
        "LEFT JOIN rc.category c " +
        "WHERE LOWER(r.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
        "OR LOWER(m.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
        "OR LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) ")
    List<Restaurant> searchRestaurants(@Param("keyword") String keyword);
}