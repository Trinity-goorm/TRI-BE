package com.trinity.ctc.domain.restaurant.entity;

import jakarta.persistence.*;

@Entity
public class RestaurantImage {

    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String url;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id")
    private Restaurant restaurant;
}
