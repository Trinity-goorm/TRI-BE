package com.trinity.ctc.domain.restaurant.entity;

import com.trinity.ctc.domain.like.entity.Likes;
import com.trinity.ctc.domain.reservation.entity.Reservation;
import com.trinity.ctc.domain.seat.entity.Seat;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

import java.util.ArrayList;
import java.util.List;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@Getter
@Entity
@NoArgsConstructor
public class Restaurant {

    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String address;
    private String phoneNumber;
    private String convenience;
    private String operatingHour;
    private String expandedDays;
    private String timeRange;
    private String caution;
    private boolean isDeleted = false;
    private int reviewCount;
    private double rating;
    private int averagePrice;

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true)
    @Fetch(FetchMode.SUBSELECT)
    private List<RestaurantImage> imageUrls = new ArrayList<>();

    @OneToMany(mappedBy = "restaurant")
    private List<Likes> likeList = new ArrayList<>();

    @OneToMany(mappedBy = "restaurant")
    private List<Reservation> reservationList = new ArrayList<>();

    @OneToMany(mappedBy = "restaurant")
    private List<Seat> seatList = new ArrayList<>();

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true)
    @Fetch(FetchMode.SUBSELECT)
    private List<RestaurantCategory> restaurantCategoryList = new ArrayList<>();

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true)
    @Fetch(FetchMode.SUBSELECT)
    private List<Menu> menus = new ArrayList<>();

    @Builder
    public Restaurant(String name, String address, String phoneNumber, String convenience, String operatingHour, String expandedDays, String timeRange, String caution, boolean isDeleted, int reviewCount, double rating, int averagePrice, List<RestaurantImage> imageUrls, List<Menu> menus) {
        this.name = name;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.convenience = convenience;
        this.operatingHour = operatingHour;
        this.expandedDays = expandedDays;
        this.timeRange = timeRange;
        this.caution = caution;
        this.isDeleted = isDeleted;
        this.reviewCount = reviewCount;
        this.rating = rating;
        this.imageUrls = (imageUrls != null) ? imageUrls : new ArrayList<>();
        this.menus = (menus != null) ? menus : new ArrayList<>();
        this.restaurantCategoryList = new ArrayList<>();
        this.averagePrice = averagePrice;
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

    public List<String> getCategories(List<RestaurantCategory> rcList) {
        return rcList.stream()
            .map(rc -> rc.getCategory().getName())
            .toList();
    }

    public List<String> getRestaurantImageUrls() {
        List<String> images = new ArrayList<>();
        for (RestaurantImage restaurantImage : this.imageUrls) {
            images.add(restaurantImage.getUrl());
        }
        return images;
    }
}
