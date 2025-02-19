package com.trinity.ctc.domain.restaurant.entity;

import com.trinity.ctc.domain.category.entity.Category;
import com.trinity.ctc.domain.restaurant.entity.compositeKey.RestaurantCategoryKey;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@Getter
public class RestaurantCategory {

    @EmbeddedId
    RestaurantCategoryKey id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("restaurantId")
    @JoinColumn(name = "Restaurant_id")
    private Restaurant restaurant;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("categoryId")
    @JoinColumn(name = "category_id")
    private Category category;

    @Builder
    public RestaurantCategory(Restaurant restaurant, Category category) {
        this.id = new RestaurantCategoryKey(restaurant.getId(), category.getId());
        this.restaurant = restaurant;
        this.category = category;
    }

    public RestaurantCategory linkToRestaurant(Restaurant restaurant) {
        this.restaurant = restaurant;
        return this;
    }
}
