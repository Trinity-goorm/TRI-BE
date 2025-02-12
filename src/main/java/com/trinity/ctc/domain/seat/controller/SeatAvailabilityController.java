package com.trinity.ctc.domain.seat.controller;

import com.trinity.ctc.domain.seat.dto.GroupedDailyAvailabilityResponse;
import com.trinity.ctc.domain.seat.service.SeatAvailabilityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/seats")
@RequiredArgsConstructor
@Slf4j
public class SeatAvailabilityController {

    private final SeatAvailabilityService seatAvailabilityService;

    /**
     * 캘린더에서 날짜 선택 시, 타임슬롯 별 가능여부(잔여좌석) 반환
     * @param restaurantId
     * @return 타임슬롯 별 가능여부(잔여좌석)
     */
    @GetMapping("/availability/day")
    public ResponseEntity<GroupedDailyAvailabilityResponse> getAvailabilityForDay(@RequestParam Long restaurantId,
                                                                                  @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate selectedDate) {
        log.info("[GetAvailabilityForDay] 대상 식당: {}, 대상 날짜: {}", restaurantId, selectedDate);
        GroupedDailyAvailabilityResponse availableSeats = seatAvailabilityService.getAvailableSeatsDay(restaurantId, selectedDate);
        return ResponseEntity.ok(availableSeats);
    }
}
