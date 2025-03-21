package com.trinity.ctc.global.loader.controller;

import com.trinity.ctc.domain.category.service.CategoryService;
import com.trinity.ctc.domain.reservation.dto.InsertReservationTimeRequest;
import com.trinity.ctc.domain.reservation.service.ReservationTimeService;
import com.trinity.ctc.domain.restaurant.service.RestaurantService;
import com.trinity.ctc.domain.seat.dto.InsertSeatTypeRequest;
import com.trinity.ctc.domain.seat.service.SeatBatchService;
import com.trinity.ctc.domain.seat.service.SeatTypeService;
import com.trinity.ctc.domain.user.service.UserDummyService;
import com.trinity.ctc.global.util.parser.CsvParser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

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
    private final CsvParser csvParser;
    private final UserDummyService userDummyService;

    @PostMapping("/order-a/crawling/categories")
    @Operation(summary = "1. 카테고리 데이터 삽입", description = "파일에서 카테고리 데이터를 읽어와 DB에 삽입합니다.")
    public ResponseEntity<String> loadCategory() {
        log.info("=== Loading categories ===");
        categoryService.insertCategoriesFromFile();
        log.info("=== Finished Loading categories ===");
        return ResponseEntity.ok("카테고리 데이터 로드 성공");
    }

    @PostMapping("/order-b/crawling/restaurants")
    @Operation(summary = "2. 크롤링된 식당 데이터 삽입", description = "파일에서 크롤링된 식당 데이터를 읽어와 DB에 삽입합니다.")
    public ResponseEntity<String> loadCrawlingRestaurant() {
        log.info("=== Loading crawling restaurants ===");
        restaurantService.insertRestaurantsFromFile();
        log.info("=== Finished Loading crawling restaurants ===");
        return ResponseEntity.ok("크롤링 식당 데이터 로드 성공.");
    }

    @PostMapping("/order-c/init/seat-type")
    @Operation(summary = "3. 좌석 유형 데이터 삽입", description = "예약좌석 유형을 DB에 삽입합니다.")
    public ResponseEntity<String> loadInitSeatType(@RequestBody List<InsertSeatTypeRequest> requests) {
        log.info("=== Loading init seatType ===");
        seatTypeService.insertInitialSeatType(requests);
        log.info("=== Finished Loading init seatType ===");
        return ResponseEntity.ok("예약좌석 데이터 세팅 성공.");
    }

    @PostMapping("/order-d/init/reservation-time")
    @Operation(summary = "4. 예약 가능 시간 슬롯 데이터 삽입", description = "예약 가능한 시간 슬롯을 DB에 삽입합니다.")
    public ResponseEntity<String> loadInitReservationTime(@RequestBody List<InsertReservationTimeRequest> requests) {
        log.info("=== Loading init reservationTime ===");
        reservationTimeService.InsertInitialReservationTimes(requests);
        log.info("=== Finished Loading init reservationTime ===");
        return ResponseEntity.ok("예약시간 타임슬롯 세팅 성공.");
    }

    @PostMapping("/order-e/init/seats")
    @Operation(summary = "5. 예약 가능 좌석 데이터 삽입", description = "이번 달과 다음 달 예약 가능 좌석 데이터를 초기화하여 삽입합니다.")
    public ResponseEntity<String> loadInitSeats() {
        log.info("=== Loading init seats ===");
        seatBatchService.batchInsertSeatDummy();
        log.info("=== Finished Loading init seats ===");
        return ResponseEntity.ok("이번달, 다음달 예약가능 초기 데이터 로드 성공.");
    }

    @Operation(summary = "CSV 업로드 후 더미 데이터 생성", description = "3개의 CSV 파일을 업로드하면 DB에 더미 데이터를 생성합니다.")
    @PostMapping(value = "/order-f/user/csv/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> loadUserCsv(
            @RequestPart("userCsv") MultipartFile userCsv,
            @RequestPart("preferenceCsv") MultipartFile preferenceCsv,
            @RequestPart("categoryCsv") MultipartFile categoryCsv
    ) {
        List<Map<String, String>> userData = csvParser.parse(userCsv);
        List<Map<String, String>> preferenceData = csvParser.parse(preferenceCsv);
        List<Map<String, String>> categoryData = csvParser.parse(categoryCsv);

        userDummyService.generateDummyData(userData, preferenceData, categoryData, 1000);
        return ResponseEntity.ok("CSV 데이터 업로드 및 DB 저장 성공!");
    }
}
