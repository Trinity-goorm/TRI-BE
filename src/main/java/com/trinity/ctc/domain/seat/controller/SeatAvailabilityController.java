package com.trinity.ctc.domain.seat.controller;

import com.trinity.ctc.domain.seat.dto.GroupedDailyAvailabilityResponse;
import com.trinity.ctc.domain.seat.service.SeatAvailabilityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RequestMapping("/api/seats")
@RequiredArgsConstructor
@Tag(name = "Seat", description = "좌석 관련 API")
@Slf4j
public class SeatAvailabilityController {

    private final SeatAvailabilityService seatAvailabilityService;

    @GetMapping("/availability/day")
    @Operation(
            summary = "날짜 선택 시, 예약가능여부 반환",
            description = "캘린더에서 날짜 선택 시, 예약시간 리스트와 가능여부, 각 예약시간별 좌석상세정보 반환"
    )
    @ApiResponse(
            responseCode = "200",
            description = "성공",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = GroupedDailyAvailabilityResponse.class)
            )
    )
    public ResponseEntity<GroupedDailyAvailabilityResponse> getAvailabilityForDay(@RequestParam Long restaurantId,
                                                                                  @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate selectedDate) {
        log.info("[GetAvailabilityForDay] 대상 식당: {}, 대상 날짜: {}", restaurantId, selectedDate);
        GroupedDailyAvailabilityResponse availableSeats = seatAvailabilityService.getAvailableSeatsDay(restaurantId, selectedDate);
        return ResponseEntity.ok(availableSeats);
    }
}
