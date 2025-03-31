package com.trinity.ctc.domain.restaurant.repository;

import com.trinity.ctc.domain.restaurant.entity.Restaurant;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

    @Query("SELECT r FROM Restaurant r JOIN r.restaurantCategoryList rc WHERE rc.category.id = :categoryId")
    Page<Restaurant> findByCategory(@Param("categoryId") Long categoryId, Pageable pageable);


    @Query("""
    SELECT r.id FROM Restaurant r
    WHERE r.name LIKE %:keyword%
       OR EXISTS (
           SELECT 1 FROM Menu m
           WHERE m.restaurant = r AND m.name LIKE %:keyword%
       )
       OR EXISTS (
           SELECT 1 FROM RestaurantCategory rc
           JOIN rc.category c
           WHERE rc.restaurant = r AND c.name LIKE %:keyword%
       )
""")
    Slice<Long> searchRestaurantIdsJPQL(@Param("keyword") String keyword, Pageable pageable);


    @Query(value = """
    SELECT r.id FROM restaurant r
    WHERE r.name LIKE %:keyword%
       OR EXISTS (
           SELECT 1 FROM menu m
           WHERE m.restaurant_id = r.id AND m.name LIKE %:keyword%
       )
       OR EXISTS (
           SELECT 1 FROM restaurant_category rc
           JOIN category c ON rc.category_id = c.id
           WHERE rc.restaurant_id = r.id AND c.name LIKE %:keyword%
       )
    ORDER BY r.id
    LIMIT :limit OFFSET :offset
""", nativeQuery = true)
    List<Long> searchRestaurantIdsNative(
        @Param("keyword") String keyword,
        @Param("limit") int limit,
        @Param("offset") int offset
    );

    @Query("SELECT r FROM Restaurant r WHERE r.id IN :ids")
    List<Restaurant> findAllByIdIn(@Param("ids") List<Long> ids);

    @Query("SELECT r FROM Restaurant r JOIN FETCH r.imageUrls WHERE r.id IN :ids")
    List<Restaurant> findAllWithImagesByIdIn(@Param("ids") List<Long> ids);
}