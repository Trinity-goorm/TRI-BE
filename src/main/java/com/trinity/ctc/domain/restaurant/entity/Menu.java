package com.trinity.ctc.domain.restaurant.entity;

import jakarta.persistence.*;

@Entity
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
}
