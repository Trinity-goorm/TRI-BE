package com.trinity.ctc.domain.restaurant.entity;

import com.trinity.ctc.domain.category.entity.Category;
import com.trinity.ctc.domain.like.entity.Likes;
import com.trinity.ctc.domain.reservation.entity.Reservation;
import com.trinity.ctc.domain.seat.entity.SeatAvailability;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Restaurant {

    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String name;
    private String address;
    private String phoneNumber;
    private String convenience;
    private String operatingHour;
    private String caution;
    private boolean isDeleted = false;
    private int reviewCount;
    private double rating;
    private double latitude;
    private double longitude;

    @OneToMany(mappedBy = "restaurant")
    private List<RestaurantImage> imageUrls = new ArrayList<>();

    @OneToMany(mappedBy = "restaurant")
    private List<Likes> likeList = new ArrayList<>();

    @OneToMany(mappedBy = "restaurant")
    private List<Reservation> reservationList = new ArrayList<>();

    @OneToMany(mappedBy = "restaurant")
    private List<SeatAvailability> seatAvailabilityList = new ArrayList<>();

    @OneToMany(mappedBy = "restaurant")
    private List<RestaurantCategory> restaurantCategoryList = new ArrayList<>();

    @OneToMany(mappedBy = "restaurant")
    private List<Menu> menus = new ArrayList<>();
}
