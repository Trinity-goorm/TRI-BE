package com.trinity.ctc.domain.like.repository;

import com.trinity.ctc.domain.like.entity.Likes;
import com.trinity.ctc.domain.restaurant.entity.Restaurant;
import com.trinity.ctc.domain.user.entity.User;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LikeRepository extends JpaRepository<Likes, Long> {
    boolean existsByUserAndRestaurant(User user, Restaurant restaurant);

    void deleteByUserAndRestaurant(User user, Restaurant restaurant);

    List<Likes> findByUser(User user);
}
