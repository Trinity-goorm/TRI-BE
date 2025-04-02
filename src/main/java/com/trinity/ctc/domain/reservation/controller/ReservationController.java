package com.trinity.ctc.domain.reservation.controller;

import com.trinity.ctc.domain.reservation.dto.PreoccupyResponse;
import com.trinity.ctc.domain.reservation.dto.ReservationRequest;
import com.trinity.ctc.domain.reservation.dto.ReservationResultResponse;
import com.trinity.ctc.domain.reservation.dto.ReservationTestRequest;
import com.trinity.ctc.domain.reservation.service.ReservationService;
import com.trinity.ctc.global.kakao.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
@Tag(name = "Reservation", description = "예약관련 API")
public class ReservationController {

    private final ReservationService reservationService;
    private final AuthService authService;

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
        Long kakaoId = Long.parseLong(authService.getAuthenticatedKakaoId());
        PreoccupyResponse result = reservationService.occupyInAdvance(kakaoId, reservationRequest.getRestaurantId(), reservationRequest.getSelectedDate(), reservationRequest.getReservationTime(), reservationRequest.getSeatTypeId());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/preoccupy/test")
    @Operation(
            summary = "예약선점 기능 락 테스트",
            description = "락을 걸었을 때의 성능 체크를 위한 테스트 API"
    )
    @ApiResponse(
            responseCode = "200",
            description = "예약선점 성공",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = PreoccupyResponse.class)
            )
    )
    public ResponseEntity<PreoccupyResponse> preoccupySeatTest(@RequestBody ReservationTestRequest reservationTestRequest) {
//        /* 비관적 락 적용 방법 */
//        PreoccupyResponse result = reservationService.occupyWithPessimisticLock(reservationTestRequest.getKakaoId(), reservationTestRequest.getRestaurantId(),
//                reservationTestRequest.getSelectedDate(), reservationTestRequest.getReservationTime(),reservationTestRequest.getSeatTypeId());

//        /* 레디스 분산 락 적용 방법 */
//        PreoccupyResponse result = reservationService.occupyWithRedisLock(reservationTestRequest.getKakaoId(), reservationTestRequest.getRestaurantId(),
//                reservationTestRequest.getSelectedDate(), reservationTestRequest.getReservationTime(),reservationTestRequest.getSeatTypeId());

        /* DB 원자적 연산 적용 방법 */
        PreoccupyResponse result = reservationService.occupyWithAtomicUpdate(reservationTestRequest.getKakaoId(), reservationTestRequest.getRestaurantId(),
                reservationTestRequest.getSelectedDate(), reservationTestRequest.getReservationTime(),reservationTestRequest.getSeatTypeId());

//        /* 레디스 원자적 연산 적용 방법 */
//        PreoccupyResponse result = reservationService.occupyWithRedisAtomic(reservationTestRequest.getKakaoId(), reservationTestRequest.getRestaurantId(),
//                reservationTestRequest.getSelectedDate(), reservationTestRequest.getReservationTime(),reservationTestRequest.getSeatTypeId());

        return ResponseEntity.ok(result);
    }

    @PostMapping("/complete")
    @Operation(
            summary = "예약(결제)완료 기능",
            description = "결제 창에서 결제를 눌렀을 때, 예약상태가 COMPLETE로 변경"
    )
    @ApiResponse(
            responseCode = "200",
            description = "예약완료 성공",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ReservationResultResponse.class)
            )
    )
    public ResponseEntity<ReservationResultResponse> completeReservation(@RequestParam long reservationId) {
        ReservationResultResponse result = reservationService.complete(reservationId);

        return ResponseEntity.ok(result);
    }

    @PostMapping("/preoccupy/cancel")
    @Operation(
            summary = "예약선점 취소 기능",
            description = "결제 창에서 타임아웃 또는 뒤로가기 시, 예약상태가 FAILED로 변경"
    )
    @ApiResponse(
            responseCode = "200",
            description = "예약선점 취소 성공",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ReservationResultResponse.class)
            )
    )
    public ResponseEntity<ReservationResultResponse> cancelPreoccupy(@RequestParam long reservationId) {
        ReservationResultResponse result = reservationService.cancelPreoccupy(reservationId);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/cancel")
    @Operation(
            summary = "완료된 예약 취소 기능",
            description = "예약시점을 검증 후 완료된 예약을 취소하는 기능. 예약상태가 CANCELED로 변경"
    )
    @ApiResponse(
            responseCode = "200",
            description = "완료된 예약 취소 성공",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ReservationResultResponse.class)
            )
    )
    public ResponseEntity<ReservationResultResponse> cancelReservation(@RequestParam long reservationId) {
        ReservationResultResponse result = reservationService.cancelReservation(reservationId);
        return ResponseEntity.ok(result);
    }
}

