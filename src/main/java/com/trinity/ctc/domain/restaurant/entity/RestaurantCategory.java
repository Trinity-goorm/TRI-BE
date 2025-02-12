package com.trinity.ctc.domain.restaurant.entity;

import com.trinity.ctc.domain.category.entity.Category;
import com.trinity.ctc.domain.restaurant.entity.compositeKey.RestaurantCategoryKey;
import jakarta.persistence.*;

@Entity
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
}
