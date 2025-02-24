package com.trinity.ctc.loader;

import com.trinity.ctc.domain.category.service.CategoryService;
import com.trinity.ctc.domain.restaurant.service.RestaurantService;
import com.trinity.ctc.domain.seat.service.SeatBatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.stereotype.Component;

@Component
@ShellComponent
@RequiredArgsConstructor
public class DataLoader {

    private final CategoryService categoryService;
    private final RestaurantService restaurantService;
    private final SeatBatchService seatBatchService;

    /**
     * 판교 식당 및 카테고리 크롤링 데이터 삽입
     */
    @ShellMethod(key = "load-crawling-data", value = "카테고리 및 레스토랑 데이터 로딩")
    public void loadDataManually() {
        System.out.println("=== crawling 데이터 로딩 시작 ===");
        categoryService.insertCategoriesFromFile();
        restaurantService.insertRestaurantsFromFile();
        System.out.println("=== crawling 데이터 로딩 완료 ===");
    }

    /**
     * 더미데이터 삽입용. 이번달, 다음달 한달치 예약정보 삽입
     */
    @ShellMethod(key = "load-seats-data", value = "Seat 더미 데이터 삽입")
    public void seatDummyInsertion() {
        System.out.println("=== Seat 데이터 생성 시작 ===");
        seatBatchService.batchInsertSeatDummy();
        System.out.println("=== Seat 데이터 생성 완료 ===");
    }
}
