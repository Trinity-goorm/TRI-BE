package com.trinity.ctc.domain.restaurant.entity;

import com.trinity.ctc.domain.category.entity.Category;
import com.trinity.ctc.domain.like.entity.Likes;
import com.trinity.ctc.domain.reservation.entity.Reservation;
import com.trinity.ctc.domain.seat.entity.SeatAvailability;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
@Getter
@Entity
@NoArgsConstructor
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

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RestaurantImage> imageUrls = new ArrayList<>();

    @OneToMany(mappedBy = "restaurant")
    private List<Likes> likeList = new ArrayList<>();

    @OneToMany(mappedBy = "restaurant")
    private List<Reservation> reservationList = new ArrayList<>();

    @OneToMany(mappedBy = "restaurant")
    private List<SeatAvailability> seatAvailabilityList = new ArrayList<>();

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RestaurantCategory> restaurantCategoryList = new ArrayList<>();

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Menu> menus = new ArrayList<>();

    @Builder
    public Restaurant(String name, String address, String phoneNumber, String convenience, String operatingHour, String caution, boolean isDeleted, int reviewCount, double rating, List<RestaurantImage> imageUrls, List<Menu> menus) {
        this.name = name;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.convenience = convenience;
        this.operatingHour = operatingHour;
        this.caution = caution;
        this.isDeleted = isDeleted;
        this.reviewCount = reviewCount;
        this.rating = rating;
        this.imageUrls = (imageUrls != null) ? imageUrls : new ArrayList<>();
        this.menus = (menus != null) ? menus : new ArrayList<>();
        this.restaurantCategoryList = new ArrayList<>();
    }

    public Restaurant addImageList(List<RestaurantImage> images) {
        for (RestaurantImage image : images) {
            this.imageUrls.add(image.linkToRestaurant(this));
        }
        return this;
    }

    public Restaurant addCategory(RestaurantCategory restaurantCategory) {
        this.restaurantCategoryList.add(restaurantCategory.linkToRestaurant(this));
        return this;
    }

    public Restaurant addMenuList(List<Menu> menus) {
        for (Menu menu : menus) {
            this.menus.add(menu.linkToRestaurant(this));
        }
        return this;
    }
}
