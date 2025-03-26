package com.trinity.ctc.domain.seat.validator;

import com.trinity.ctc.global.exception.CustomException;
import com.trinity.ctc.global.exception.error_code.SeatErrorCode;

public class CapacityValidator {

    private CapacityValidator() {
    }

    public static void isValid(int minCapacity, int maxCapacity, int availableSeats) {
        if (minCapacity < 0 || maxCapacity < 0 || availableSeats < 0) {
            throw new CustomException(SeatErrorCode.CAPACITY_IS_NEGATIVE);
        }
        if (minCapacity > maxCapacity) {
            throw new CustomException(SeatErrorCode.BIGGER_MIN_CAPACITY);
        }
    }

    public static void validateAvailableSeats(int availableSeats) {
        if (availableSeats < 0) {
            throw new CustomException(SeatErrorCode.CAPACITY_IS_NEGATIVE);
        } else if (availableSeats == 0) {
            throw new CustomException(SeatErrorCode.NO_AVAILABLE_SEAT);
        }
    }

    public static void validateNegativeSeats(int availableSeats) {
        if (availableSeats < 0) {
            throw new CustomException(SeatErrorCode.CAPACITY_IS_NEGATIVE);
        }
    }

    public static boolean checkEmptySeat(int availableSeatsBefore, int availableSeatsAfter) {
        return availableSeatsBefore == 0 && availableSeatsAfter == 1;
    }
}
