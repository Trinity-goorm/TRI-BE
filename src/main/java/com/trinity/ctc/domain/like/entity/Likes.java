package com.trinity.ctc.domain.like.entity;

import com.trinity.ctc.domain.restaurant.entity.Restaurant;
import com.trinity.ctc.domain.user.entity.User;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class Likes {

    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id")
    private Restaurant restaurant;
}
