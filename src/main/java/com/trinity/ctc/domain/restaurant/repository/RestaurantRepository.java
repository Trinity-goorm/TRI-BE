package com.trinity.ctc.domain.restaurant.repository;

import com.trinity.ctc.domain.restaurant.entity.Restaurant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

    @Query("SELECT r FROM Restaurant r JOIN r.restaurantCategoryList rc WHERE rc.category.id = :categoryId")
    Page<Restaurant> findByCategory(@Param("categoryId") Long categoryId, Pageable pageable);

    @Query("SELECT DISTINCT r FROM Restaurant r " +
            "LEFT JOIN r.menus m " +
            "LEFT JOIN r.restaurantCategoryList rc " +
            "LEFT JOIN rc.category c " +
            "WHERE (LOWER(r.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(m.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND r.averagePrice>5000")
    Page<Restaurant> searchRestaurants(@Param("keyword") String keyword, Pageable pageable);
}