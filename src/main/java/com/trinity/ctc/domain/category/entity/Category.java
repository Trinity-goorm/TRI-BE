package com.trinity.ctc.domain.category.entity;

import com.trinity.ctc.domain.restaurant.entity.RestaurantCategory;
import com.trinity.ctc.domain.user.entity.UserPreferenceCategory;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Category {

    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String name;
    private Boolean isDeleted;

    @OneToMany(mappedBy = "category")
    private List<UserPreferenceCategory> userPreferenceCategories = new ArrayList<>();

    @OneToMany(mappedBy = "category")
    private List<RestaurantCategory> restaurantCategoryList = new ArrayList<>();
}
