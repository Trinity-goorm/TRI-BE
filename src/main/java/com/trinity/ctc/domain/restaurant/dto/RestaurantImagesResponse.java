package com.trinity.ctc.domain.restaurant.dto;

import lombok.Getter;

@Getter
public class RestaurantImagesResponse {
    private String url;


    public RestaurantImagesResponse(String url) {
        this.url = url;
    }
}
