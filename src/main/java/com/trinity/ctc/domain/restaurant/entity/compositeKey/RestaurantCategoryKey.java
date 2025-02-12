package com.trinity.ctc.domain.restaurant.entity.compositeKey;

import jakarta.persistence.*;

@Embeddable
public class RestaurantCategoryKey {

    @Column(name = "restaurant_id")
    Long restaurantId;

    @Column(name = "category_id")
    Long categoryId;
}
