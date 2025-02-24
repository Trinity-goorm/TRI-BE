package com.trinity.ctc.domain.restaurant.entity.compositeKey;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class RestaurantCategoryKey {

    @Column(name = "restaurant_id")
    Long restaurantId;

    @Column(name = "category_id")
    Long categoryId;
}
