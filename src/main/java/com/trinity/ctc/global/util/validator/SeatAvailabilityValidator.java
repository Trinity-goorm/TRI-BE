package com.trinity.ctc.global.util.validator;

import com.trinity.ctc.domain.seat.entity.Seat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class SeatAvailabilityValidator {
    private SeatAvailabilityValidator() {}

    public static boolean validate(Seat seat, boolean isToday) {
        return isToday ? checkAvailabilityForToday(seat) : checkAvailabilityForFuture(seat);
    }

    public static boolean isAnySeatAvailable(List<Seat> seats, boolean isToday) {
        return seats.stream()
                .anyMatch(sa -> SeatAvailabilityValidator.validate(sa, isToday));
    }

    public static boolean checkAvailability(Seat seat) {
        LocalDate reservationDate = seat.getReservationDate();
        DateTimeValidator.isPast(reservationDate);
        boolean isToday = DateTimeValidator.isToday(reservationDate);
        return isToday ? checkAvailabilityForToday(seat) : checkAvailabilityForFuture(seat);
    }

    private static boolean checkAvailabilityForToday(Seat seat) {
        LocalTime oneHourLater = LocalTime.now().plusHours(1);
        return seat.getAvailableSeats() > 0 &&
                seat.getReservationTime().getTimeSlot().isAfter(oneHourLater);
    }

    private static boolean checkAvailabilityForFuture(Seat seat) {
        return seat.getAvailableSeats() > 0;
    }
}
