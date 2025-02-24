package com.trinity.ctc.domain.restaurant.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@Getter
public class RestaurantImage {

    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(columnDefinition = "TEXT")
    private String url;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id")
    private Restaurant restaurant;

    @Builder
    public RestaurantImage(String url) {
        if (url.equals("이미지 정보 없음")){
            this.url = null;
        }
        else{
            this.url = url;
        }
    }

    public RestaurantImage linkToRestaurant(Restaurant restaurant) {
        this.restaurant = restaurant;
        return this;
    }

}
