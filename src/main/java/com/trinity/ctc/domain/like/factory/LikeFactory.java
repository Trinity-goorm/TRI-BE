package com.trinity.ctc.domain.like.factory;

import com.trinity.ctc.domain.like.entity.Likes;
import com.trinity.ctc.domain.restaurant.repository.RestaurantRepository;
import com.trinity.ctc.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class LikeFactory {

    private final RestaurantRepository restaurantRepository;
    private final UserRepository userRepository;

    public List<Likes> createLikesByCsv(List<Map<String, String>> csvLikesData) {
        List<Likes> likes = new ArrayList<>();
        for (Map<String, String> row : csvLikesData) {
            Likes like = Likes.builder()
                    .user(userRepository.findById(Long.parseLong(row.get("user_id"))).orElse(null))
                    .restaurant(restaurantRepository.findById(Long.parseLong(row.get("restaurant_id"))).orElse(null))
                    .createdAt(LocalDateTime.parse(row.get("created_at")))
                    .build();

            likes.add(like);
        }
        return likes;
    }
}