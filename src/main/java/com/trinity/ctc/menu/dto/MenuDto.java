package com.trinity.ctc.menu.dto;

import com.trinity.ctc.domain.restaurant.entity.Menu;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MenuDto {
    private String name;
    private int price;

    public static MenuDto fromEntity(Menu menu) {
        return MenuDto.builder()
            .name(menu.getName())
            .price(menu.getPrice())
            .build();
    }
}
