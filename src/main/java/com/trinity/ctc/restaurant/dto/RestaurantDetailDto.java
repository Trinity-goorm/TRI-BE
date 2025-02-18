package com.trinity.ctc.restaurant.dto;

import com.trinity.ctc.domain.restaurant.entity.Restaurant;

import com.trinity.ctc.domain.restaurant.entity.RestaurantImage;
import com.trinity.ctc.menu.dto.MenuDto;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class RestaurantDetailDto {

    private Long restaurantId;
    private String name;
    private List<String> imageUrls;
    private String location;
    private String category;
    private double rating;
    private String operatingHours;
    private List<String> facilities;
    private List<String> cautions;
    private List<MenuDto> menus;
    private int wishCount;

    public static RestaurantDetailDto fromEntity(Restaurant restaurant) {
        //식당 평균 가격대 추가
        // 식당 운영시간
        return RestaurantDetailDto.builder()
            .restaurantId(restaurant.getId())
            .name(restaurant.getName())
            .imageUrls(restaurant.getImageUrls().stream().map(RestaurantImage::getUrl).collect(Collectors.toList()))
            .location(restaurant.getAddress())
            .category(restaurant.getRestaurantCategoryList().stream()
                .map(rc -> rc.getCategory().getName())
                .collect(Collectors.joining(", ")))
            .rating(restaurant.getRating())
            .operatingHours(restaurant.getOperatingHour())
            .facilities(List.of(restaurant.getConvenience().split("\n")))
            .cautions(List.of(restaurant.getCaution().split(", ")))
            .menus(restaurant.getMenus().stream().map(MenuDto::fromEntity).collect(Collectors.toList()))
            .wishCount(restaurant.getLikeList().size())
            .build();
    }
}

