package com.trinity.ctc.domain.reservation.controller;

import com.trinity.ctc.domain.reservation.dto.PreoccupyResponse;
import com.trinity.ctc.domain.reservation.dto.ReservationRequest;
import com.trinity.ctc.domain.reservation.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
@Tag(name = "Reservation", description = "예약관련 API")
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping("/preoccupy")
    @Operation(
            summary = "예약선점 기능",
            description = "예약하기 전, 예약정보 기반으로 예약자리를 선점하는 기능"
    )
    @ApiResponse(
            responseCode = "200",
            description = "예약선점 성공",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = PreoccupyResponse.class)
            )
    )
    public ResponseEntity<PreoccupyResponse> preoccupySeat(@RequestBody ReservationRequest reservationRequest) {
        PreoccupyResponse result = reservationService.occupyInAdvance(reservationRequest);
        return ResponseEntity.ok(result);
    }
}
