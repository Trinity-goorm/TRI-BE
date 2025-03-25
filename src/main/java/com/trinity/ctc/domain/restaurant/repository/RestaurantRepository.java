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

    @Query("SELECT r FROM Restaurant r " +
            "JOIN r.menus m " +
            "JOIN r.restaurantCategoryList rc " +
            "JOIN rc.category c " +
            "WHERE ((r.name) LIKE (CONCAT('%', :keyword, '%')) " +
            "OR (m.name) LIKE (CONCAT('%', :keyword, '%')) " +
            "OR (c.name) LIKE (CONCAT('%', :keyword, '%'))) ")
    Page<Restaurant> searchRestaurants(@Param("keyword") String keyword, Pageable pageable);
}