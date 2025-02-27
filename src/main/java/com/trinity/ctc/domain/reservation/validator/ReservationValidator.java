package com.trinity.ctc.domain.reservation.validator;

import com.trinity.ctc.domain.reservation.dto.ReservationRequest;
import com.trinity.ctc.domain.reservation.entity.Reservation;
import com.trinity.ctc.domain.reservation.repository.ReservationRepository;
import com.trinity.ctc.domain.reservation.status.ReservationStatus;
import com.trinity.ctc.global.exception.CustomException;
import com.trinity.ctc.global.exception.error_code.ReservationErrorCode;
import com.trinity.ctc.global.util.validator.DateTimeValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ReservationValidator {

    private final ReservationRepository reservationRepository;

    public static void validateReservationUserMatched(long reservationUserId, long requestUserid) {
        if (reservationUserId != requestUserid) {
            throw new CustomException(ReservationErrorCode.RESERVATION_USER_MISMATCH);
        }
    }

    public static void isPreoccupied(ReservationStatus reservationStatus) {
        if (reservationStatus != ReservationStatus.IN_PROGRESS) {
            throw new CustomException(ReservationErrorCode.NOT_PREOCCUPIED);
        }
    }

    public static void isCompleted(ReservationStatus reservationStatus) {
        if (reservationStatus != ReservationStatus.COMPLETED) {
            throw new CustomException(ReservationErrorCode.NOT_COMPLETED);
        }
    }

    public static boolean checkCOD(LocalDate reservationDate) {
        return DateTimeValidator.isMoreThanOneDayAway(reservationDate);
    }

    public void validateUserReservation(ReservationRequest request) {
        if (hasExistingReservation(request)) {
            throw new CustomException(ReservationErrorCode.ALREADY_RESERVED_BY_USER);
        }
    }

    /**
     * 사용자 예약정보 검증 v1 (사용자 필터링 후 예약정보로 확인)
     * @param request
     * @return
     */
    private boolean hasExistingReservation(ReservationRequest request) {
        List<Reservation> userReservations = reservationRepository.findByUserIdAndStatusIn(
                request.getUserId(),
                List.of(ReservationStatus.IN_PROGRESS, ReservationStatus.COMPLETED)
        );

        return userReservations.stream()
                .anyMatch(reservation ->
                        reservation.getRestaurant().getId().equals(request.getRestaurantId()) &&
                                reservation.getReservationDate().equals(request.getSelectedDate()) &&
                                reservation.getReservationTime().getTimeSlot().equals(request.getReservationTime()) &&
                                reservation.getSeatType().getId().equals(request.getSeatTypeId())
                );
    }
}
