package com.trinity.ctc.domain.reservation.service;

import com.trinity.ctc.domain.reservation.dto.PreoccupyResponse;
import com.trinity.ctc.domain.reservation.dto.ReservationRequest;
import com.trinity.ctc.domain.reservation.entity.Reservation;
import com.trinity.ctc.domain.reservation.entity.ReservationTime;
import com.trinity.ctc.domain.reservation.repository.ReservationRepository;
import com.trinity.ctc.domain.reservation.repository.ReservationTimeRepository;
import com.trinity.ctc.domain.reservation.status.ReservationStatus;
import com.trinity.ctc.domain.restaurant.entity.Restaurant;
import com.trinity.ctc.domain.restaurant.repository.RestaurantRepository;
import com.trinity.ctc.domain.seat.entity.SeatAvailability;
import com.trinity.ctc.domain.seat.entity.SeatType;
import com.trinity.ctc.domain.seat.repository.SeatAvailabilityRepository;
import com.trinity.ctc.domain.seat.repository.SeatTypeRepository;
import com.trinity.ctc.domain.user.entity.User;
import com.trinity.ctc.kakao.repository.UserRepository;
import com.trinity.ctc.util.exception.CustomException;
import com.trinity.ctc.util.exception.error_code.*;
import com.trinity.ctc.util.validator.SeatAvailabilityValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationService {
    private final SeatAvailabilityRepository seatAvailabilityRepository;
    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final SeatTypeRepository seatTypeRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ReservationRepository reservationRepository;

    /**
     * 선점하기
     * @param reservationRequest
     * @return 선점성공여부
     */
    @Transactional
    public PreoccupyResponse occupyInAdvance(ReservationRequest reservationRequest) {
        log.info("[예약 요청] 날짜: {}, 시간: {}, 레스토랑 ID: {}, 사용자 ID: {}, 좌석 타입 ID: {}",
                reservationRequest.getSelectedDate(), reservationRequest.getReservationTime(),
                reservationRequest.getRestaurantId(), reservationRequest.getUserId(), reservationRequest.getSeatTypeId());

        // 검증 (좌석 남은 자리 확인)
        SeatAvailability seatAvailability = validateSeatAvailability(reservationRequest);

        // 좌석 한개 선점
        log.info("[좌석 선점] 기존 좌석 수: {}", seatAvailability.getAvailableSeats());
        seatAvailability.preoccupyOneSeat();
        log.info("[좌석 선점 완료] 남은 좌석 수: {}", seatAvailability.getAvailableSeats());

        // 예약정보 생성 -> 저장
        Reservation reservation = createReservation(reservationRequest);
        reservationRepository.save(reservation);

        log.info("[예약 성공] 예약 ID: {}", reservation.getId());

        // DTO 생성
        return PreoccupyResponse.of(true, reservation.getId());
    }

    /**
     * 선점가능상태 검증
     * @param reservationRequest
     * @return 선점대상 좌석정보
     */
    private SeatAvailability validateSeatAvailability(ReservationRequest reservationRequest) {
        SeatAvailability seatAvailability = seatAvailabilityRepository.findByReservationData(
                reservationRequest.getRestaurantId(),
                reservationRequest.getSelectedDate(),
                reservationRequest.getReservationTime(),
                reservationRequest.getSeatTypeId()
        );

        if (!SeatAvailabilityValidator.checkAvailability(seatAvailability)) {
            log.warn("[예약 실패] 좌석 부족 - 레스토랑 ID: {}, 날짜: {}, 시간: {}, 좌석 타입 ID: {}",
                    reservationRequest.getRestaurantId(), reservationRequest.getSelectedDate(),
                    reservationRequest.getReservationTime(), reservationRequest.getSeatTypeId());
            throw new CustomException(SeatErrorCode.NO_AVAILABLE_SEAT);
        }

        log.info("[좌석 확인 완료] 남은 좌석 수: {}", seatAvailability.getAvailableSeats());
        return seatAvailability;
    }

    /**
     * 예약정보 생성
     * @param reservationRequest
     * @return 예약정보
     */
    private Reservation createReservation(ReservationRequest reservationRequest) {
        User user = userRepository.findById(reservationRequest.getUserId())
                .orElseThrow(() -> new CustomException(UserErrorCode.NOT_FOUND));

        Restaurant restaurant = restaurantRepository.findById(reservationRequest.getRestaurantId())
                .orElseThrow(() -> new CustomException(RestaurantErrorCode.NOT_FOUND));

        SeatType seatType = seatTypeRepository.findById(reservationRequest.getSeatTypeId())
                .orElseThrow(() -> new CustomException(SeatTypeErrorCode.NOT_FOUND));

        ReservationTime reservationTime = reservationTimeRepository.findByTimeSlot(reservationRequest.getReservationTime())
                .orElseThrow(() -> new CustomException(ReservationTimeErrorCode.NOT_FOUND));

        return Reservation.builder()
                .reservationDate(reservationRequest.getSelectedDate())
                .status(ReservationStatus.IN_PROGRESS)
                .restaurant(restaurant)
                .user(user)
                .reservationTime(reservationTime)
                .seatType(seatType)
                .build();
    }
}
