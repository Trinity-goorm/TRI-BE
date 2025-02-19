package com.trinity.ctc.domain.restaurant.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@Getter
public class Menu {

    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String name;
    private int price;
    private String imageUrl;
    private boolean isActive = true;
    private boolean isDeleted;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id")
    private Restaurant restaurant;

    @Builder
    public Menu(String name, int price, String imageUrl, boolean isActive, boolean isDeleted, Restaurant restaurant) {
        this.name = name;
        this.price = price;
        this.imageUrl = imageUrl;
        this.isActive = isActive;
        this.isDeleted = isDeleted;
        this.restaurant = restaurant;
    }


    public Menu linkToRestaurant(Restaurant restaurant) {
        this.restaurant = restaurant;
        return this;
    }
}
