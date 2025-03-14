package com.trinity.ctc.domain.like.service;

import com.trinity.ctc.domain.like.entity.Likes;
import com.trinity.ctc.domain.like.repository.LikeRepository;
import com.trinity.ctc.domain.restaurant.dto.RestaurantDetailResponse;
import com.trinity.ctc.domain.restaurant.entity.Restaurant;
import com.trinity.ctc.domain.restaurant.repository.RestaurantRepository;
import com.trinity.ctc.domain.user.entity.User;
import com.trinity.ctc.domain.user.repository.UserRepository;
import com.trinity.ctc.global.exception.CustomException;
import com.trinity.ctc.global.exception.error_code.LikeErrorCode;
import com.trinity.ctc.global.exception.error_code.UserErrorCode;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
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
    public void likeRestaurant(String kakaoId, Long restaurantId) {
        Optional<User> userOptional = userRepository.findByKakaoId(Long.valueOf(kakaoId));
        User user = userOptional.orElseThrow(() -> new CustomException(UserErrorCode.NOT_FOUND));

        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new CustomException(LikeErrorCode.RESTAURANT_NOT_FOUND));

        if (likeRepository.existsByUserAndRestaurant(user, restaurant)) {
            throw new CustomException(LikeErrorCode.ALREADY_LIKED);
        }

        Likes likes = Likes.builder()
                .user(user)
                .restaurant(restaurant)
                .createdAt(LocalDateTime.now())
                .build();

        likeRepository.save(likes);
    }

    @Transactional
    public void unlikeRestaurant(String kakaoId, Long restaurantId) {
        Optional<User> userOptional = userRepository.findByKakaoId(Long.valueOf(kakaoId));
        User user = userOptional.orElseThrow(() -> new CustomException(UserErrorCode.NOT_FOUND));

        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new CustomException(LikeErrorCode.RESTAURANT_NOT_FOUND));

        if (!likeRepository.existsByUserAndRestaurant(user, restaurant)) {
            throw new CustomException(LikeErrorCode.NOT_LIKED);
        }

        likeRepository.deleteByUserAndRestaurant(user, restaurant);
    }

    @Transactional
    public List<RestaurantDetailResponse> getLikeList(String kakaoId) {
        Optional<User> userOptional = userRepository.findByKakaoId(Long.valueOf(kakaoId));
        User user = userOptional.orElseThrow(() -> new CustomException(UserErrorCode.NOT_FOUND));

        List<Likes> likes = likeRepository.findByUser(user);

        return likes.stream()
                .map(like -> RestaurantDetailResponse.fromLike(like.getRestaurant()))
                .collect(Collectors.toList());
    }
}

