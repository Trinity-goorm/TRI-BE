package com.trinity.ctc.util.validator;

import com.trinity.ctc.domain.reservation.status.ReservationStatus;
import com.trinity.ctc.util.exception.CustomException;
import com.trinity.ctc.util.exception.error_code.ReservationErrorCode;

import java.time.LocalDate;

public class ReservationValidator {

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
        return DateTimeValidator.isTwoDaysAgoOrBefore(reservationDate);
    }
}
