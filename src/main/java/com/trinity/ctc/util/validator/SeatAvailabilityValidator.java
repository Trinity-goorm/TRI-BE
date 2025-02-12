package com.trinity.ctc.util.validator;

import com.trinity.ctc.domain.seat.entity.SeatAvailability;

import java.time.LocalTime;

public class SeatAvailabilityValidator {
    private SeatAvailabilityValidator() {}

    public static boolean validate(SeatAvailability seatAvailability, boolean isToday) {
        return isToday ? checkAvailabilityForToday(seatAvailability) : checkAvailabilityForFuture(seatAvailability);
    }

    private static boolean checkAvailabilityForToday(SeatAvailability seatAvailability) {
        LocalTime oneHourLater = LocalTime.now().plusHours(1);
        return seatAvailability.getAvailableSeats() > 0 &&
                seatAvailability.getReservationTime().getTimeSlot().isAfter(oneHourLater);
    }

    private static boolean checkAvailabilityForFuture(SeatAvailability seatAvailability) {
        return seatAvailability.getAvailableSeats() > 0;
    }
}
