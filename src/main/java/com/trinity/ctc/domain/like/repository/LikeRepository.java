package com.trinity.ctc.domain.like.repository;

import com.trinity.ctc.domain.like.entity.Likes;
import com.trinity.ctc.domain.restaurant.entity.Restaurant;
import com.trinity.ctc.domain.user.entity.User;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface LikeRepository extends JpaRepository<Likes, Long> {
    boolean existsByUserAndRestaurant(User user, Restaurant restaurant);

    void deleteByUserAndRestaurant(User user, Restaurant restaurant);

    List<Likes> findByUser(User user);

    @Query("SELECT l.restaurant.id FROM Likes l WHERE l.user = :user AND l.restaurant.id IN :restaurantIds")
    List<Long> findLikedRestaurantIds(@Param("user") User user, @Param("restaurantIds") List<Long> restaurantIds);
}

