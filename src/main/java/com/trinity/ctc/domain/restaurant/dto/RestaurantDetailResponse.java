package com.trinity.ctc.domain.restaurant.dto;

import com.trinity.ctc.domain.restaurant.entity.Restaurant;

import com.trinity.ctc.domain.restaurant.entity.RestaurantImage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
@Schema(description = "식당 상세 정보 반환")
public class RestaurantDetailResponse {

    @Schema(description = "식당 ID", example = "1")
    private Long restaurantId;

    @Schema(description = "식당 이름", example = "이가네양꼬치 판교본점")
    private String name;

    @Schema(description = "식당 이미지 URL 리스트", example = "[\"https://catchping.com/image1.jpg\", \"https://catchping.com/image2.jpg\"]")
    private List<String> imageUrls;

    @Schema(description = "식당 위치", example = "경기 성남시 분당구 분당내곡로 155 KCC웰츠타워 104호")
    private String location;

    @Schema(description = "식당 카테고리", example = "중식")
    private String category;

    @Schema(description = "식당 평점", example = "0.0")
    private double rating;

    @Schema(description = "평균 가격", example = "15166")
    private int averagePrice;

    @Schema(description = "운영 요일", example = "월,화,수,목,금,토,일")
    private String expandedDays;

    @Schema(description = "운영 시간", example = "12:00 ~ 24:00")
    private String timeRange;

    @Schema(description = "편의시설", example = "[\n"
        + "        \"WIFI\",\n"
        + "        \"동물출입\",\n"
        + "        \"주차\",\n"
        + "        \"휠체어사용\",\n"
        + "        \"놀이방\",\n"
        + "        \"흡연실\"\n"
        + "    ]")
    private List<String> facilities;

    @Schema(description = "주의사항", example = "[\n"
        + "        \"예약가능\",\n"
        + "        \"배달불가\",\n"
        + "        \"포장가능\"\n"
        + "    ]")
    private List<String> cautions;

    @Schema(description = "메뉴 리스트", example = "[\n"
        + "        {\n"
        + "            \"name\": \"대하구이\",\n"
        + "            \"price\": 13000\n"
        + "        },\n"
        + "        {\n"
        + "            \"name\": \"탕수육\",\n"
        + "            \"price\": 21000\n"
        + "        }]")
    private List<MenuDto> menus;

    @Schema(description = "찜 수", example = "0")
    private int wishCount;

    public static RestaurantDetailResponse fromEntity(Restaurant restaurant) {
        // 식당 운영시간
        return RestaurantDetailResponse.builder()
            .restaurantId(restaurant.getId())
            .name(restaurant.getName())
            .imageUrls(restaurant.getImageUrls().stream().map(RestaurantImage::getUrl).collect(Collectors.toList()))
            .location(restaurant.getAddress())
            .category(restaurant.getRestaurantCategoryList().stream()
                .map(rc -> rc.getCategory().getName())
                .collect(Collectors.joining(", ")))
            .rating(restaurant.getRating())
            .averagePrice(restaurant.getAveragePrice())
            .expandedDays(restaurant.getExpandedDays())
            .timeRange(restaurant.getTimeRange())
            .facilities(List.of(restaurant.getConvenience().split("\n")))
            .cautions(List.of(restaurant.getCaution().split(", ")))
            .menus(restaurant.getMenus().stream().map(MenuDto::fromEntity).collect(Collectors.toList()))
            .wishCount(restaurant.getLikeList().size())
            .build();
    }

    public static RestaurantDetailResponse fromLike(Restaurant restaurant) {
        return RestaurantDetailResponse.builder()
            .restaurantId(restaurant.getId())
            .name(restaurant.getName())
            .imageUrls(restaurant.getImageUrls().stream().map(RestaurantImage::getUrl).collect(Collectors.toList()))
            .location(restaurant.getAddress())
            .averagePrice(restaurant.getAveragePrice())
            .category(restaurant.getRestaurantCategoryList().stream()
                .map(rc -> rc.getCategory().getName())
                .collect(Collectors.joining(", ")))
            .rating(restaurant.getRating())
            .build();
    }
}

