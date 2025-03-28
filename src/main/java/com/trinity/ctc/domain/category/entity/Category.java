package com.trinity.ctc.domain.category.entity;

import com.trinity.ctc.domain.restaurant.entity.RestaurantCategory;
import com.trinity.ctc.domain.user.entity.UserPreferenceCategory;
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

@Entity
@NoArgsConstructor
@Getter
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

    @Builder
    public Category(String name, Boolean isDeleted) {
        this.name = name;
        this.isDeleted = isDeleted;
    }
}
