package com.trinity.ctc.domain.restaurant.dto;

import lombok.Getter;

@Getter
public class RestaurantPreviewRequest {
    private int page;
    private String sortType;
    private Long userId;

    public RestaurantPreviewRequest(int page, String sortType, Long userId) {
        this.page = (page < 1) ? 1 : page; // 페이지 번호 최소 1 보장
        this.sortType = (sortType == null || sortType.isEmpty()) ? "highest_price" : sortType;
        this.userId = userId;
    }

}
