package com.trinity.ctc.loader.controller;

import com.trinity.ctc.domain.category.service.CategoryService;
import com.trinity.ctc.domain.reservation.dto.InsertReservationTimeRequest;
import com.trinity.ctc.domain.reservation.service.ReservationTimeService;
import com.trinity.ctc.domain.restaurant.service.RestaurantService;
import com.trinity.ctc.domain.seat.dto.InsertSeatTypeRequest;
import com.trinity.ctc.domain.seat.service.SeatBatchService;
import com.trinity.ctc.domain.seat.service.SeatTypeService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/data")
@Tag(name = "DataLoader", description = "데이터 삽입 관련 APIs")
public class DataController {

    private final SeatBatchService seatBatchService;
    private final CategoryService categoryService;
    private final RestaurantService restaurantService;
    private final SeatTypeService seatTypeService;
    private final ReservationTimeService reservationTimeService;

    @PostMapping("/crawling/categories")
    public ResponseEntity<String> loadCategory() {
        log.info("=== Loading categories ===");
        categoryService.insertCategoriesFromFile();
        log.info("=== Finished Loading categories ===");
        return ResponseEntity.ok("✅ 카테고리 데이터 로드 성공");
    }

    @PostMapping("/crawling/restaurants")
    public ResponseEntity<String> loadCrawlingRestaurant() {
        log.info("=== Loading crawling restaurants ===");
        restaurantService.insertRestaurantsFromFile();
        log.info("=== Finished Loading crawling restaurants ===");
        return ResponseEntity.ok("크롤링 식당 데이터 로드 성공.");
    }

    @PostMapping("/init/seats")
    public ResponseEntity<String> loadInitSeats() {
        log.info("=== Loading init seats ===");
        seatBatchService.batchInsertSeatDummy();
        log.info("=== Finished Loading init seats ===");
        return ResponseEntity.ok("이번달, 다음달 예약가능 초기 데이터 로드 성공.");
    }

    @PostMapping("/init/seatType")
    public ResponseEntity<String> loadInitSeatType(@RequestBody List<InsertSeatTypeRequest> requests) {
        log.info("=== Loading init seatType ===");
        seatTypeService.insertInitialSeatType(requests);
        log.info("=== Finished Loading init seatType ===");
        return ResponseEntity.ok("예약좌석 데이터 세팅 성공.");
    }

    @PostMapping("/init/reservationTime")
    public ResponseEntity<String> loadInitReservationTime(@RequestBody List<InsertReservationTimeRequest> requests) {
        log.info("=== Loading init reservationTime ===");
        reservationTimeService.InsertInitialReservationTimes(requests);
        log.info("=== Finished Loading init reservationTime ===");
        return ResponseEntity.ok("예약시간 타임슬롯 세팅 성공.");
    }
}
