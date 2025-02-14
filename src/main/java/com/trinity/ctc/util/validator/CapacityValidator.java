package com.trinity.ctc.util.validator;

import com.trinity.ctc.util.exception.CustomException;
import com.trinity.ctc.util.exception.error_code.SeatErrorCode;

public class CapacityValidator {

    private CapacityValidator() {}

    public static void isValid(int minCapacity, int maxCapacity, int availableSeats) {
        if (minCapacity < 0 || maxCapacity < 0 || availableSeats < 0) {
            throw new CustomException(SeatErrorCode.CAPACITY_IS_NEGATIVE);
        }
        if (minCapacity > maxCapacity) {
            throw new CustomException(SeatErrorCode.BIGGER_MIN_CAPACITY);
        }
    }
}
