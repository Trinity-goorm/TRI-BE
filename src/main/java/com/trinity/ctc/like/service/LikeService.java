package com.trinity.ctc.like.service;

import com.trinity.ctc.domain.like.entity.Likes;
import com.trinity.ctc.domain.restaurant.entity.Restaurant;
import com.trinity.ctc.domain.user.entity.User;
import com.trinity.ctc.kakao.repository.UserRepository;
import com.trinity.ctc.like.repository.LikeRepository;
import com.trinity.ctc.restaurant.dto.RestaurantDetailDto;
import com.trinity.ctc.restaurant.repository.RestaurantRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LikeService {

    private final LikeRepository likeRepository;
    private final RestaurantRepository restaurantRepository;
    private final UserRepository userRepository;

    @Transactional
    public void likeRestaurant(Long userId, Long restaurantId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("해당 유저를 찾을 수 없습니다. ID: " + userId));
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
            .orElseThrow(() -> new IllegalArgumentException("해당 식당을 찾을 수 없습니다. ID: " + restaurantId));

        if (likeRepository.existsByUserAndRestaurant(user, restaurant)) {
            throw new IllegalStateException("이미 찜한 식당입니다.");
        }

        Likes likes = Likes.builder()
                .user(user)
                    .restaurant(restaurant)
                        .createdAt(LocalDateTime.now())
                            .build();

        likeRepository.save(likes);
    }

    @Transactional
    public void unlikeRestaurant(Long userId, Long restaurantId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("해당 유저를 찾을 수 없습니다. ID: " + userId));
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
            .orElseThrow(() -> new IllegalArgumentException("해당 식당을 찾을 수 없습니다. ID: " + restaurantId));

        if (!likeRepository.existsByUserAndRestaurant(user, restaurant)) {
            throw new IllegalStateException("찜하지 않은 식당입니다.");
        }

        likeRepository.deleteByUserAndRestaurant(user, restaurant);
    }

    @Transactional
    public List<RestaurantDetailDto> getLikeList(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("해당 유저를 찾을 수 없습니다. ID: " + userId));

        List<Likes> likes = likeRepository.findByUser(user);

        return likes.stream()
            .map(like -> RestaurantDetailDto.fromLike(like.getRestaurant()))
            .collect(Collectors.toList());
    }
}

