package com.trinity.ctc.domain.restaurant.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "식당 카테고리 이름")
public class RestaurantCategoryName {

    @Schema(description = "식당 ID", example = "1")
    private Long restaurantId;

    @Schema(description = "식당 카테고리 이름", example = "한식")
    private String categoryName;

}
