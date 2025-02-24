package com.trinity.ctc.domain.restaurant.dto;

import com.trinity.ctc.domain.restaurant.entity.Menu;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "메뉴 정보 반환")
public class MenuDto {

    @Schema(description = "메뉴 이름", example = "순두부찌개")
    private String name;

    @Schema(description = "메뉴 가격", example = "8000")
    private int price;

    public static MenuDto fromEntity(Menu menu) {
        return MenuDto.builder()
            .name(menu.getName())
            .price(menu.getPrice())
            .build();
    }

    public static int getPrices(Menu menu) {
        return menu.getPrice();
    }
}
