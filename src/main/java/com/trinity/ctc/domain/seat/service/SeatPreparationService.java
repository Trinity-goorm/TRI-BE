package com.trinity.ctc.domain.seat.service;

import com.google.common.annotations.VisibleForTesting;
import com.trinity.ctc.domain.reservation.entity.ReservationTime;
import com.trinity.ctc.domain.restaurant.entity.Restaurant;
import com.trinity.ctc.domain.seat.entity.Seat;
import com.trinity.ctc.domain.seat.entity.SeatType;
import com.trinity.ctc.domain.seat.mode.DateRangeMode;
import com.trinity.ctc.domain.seat.service.provider.SeatDataProvider;
import com.trinity.ctc.domain.seat.strategy.DateRangeCalculator;
import com.trinity.ctc.domain.seat.strategy.DateRangeCalculatorFactory;
import com.trinity.ctc.global.records.DateRange;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SeatPreparationService {

    private final SeatDataProvider seatDataProvider;
    private final DateRangeCalculatorFactory dateRangeCalculatorFactory;

    public List<Seat> prepareSeatsData(DateRangeMode mode, int availableSeatCount) {
        DateRangeCalculator dateRangeCalculator = dateRangeCalculatorFactory.getCalculator(mode);
        DateRange dateRange = dateRangeCalculator.calculateDateRange();

        return generateSeats(dateRange, availableSeatCount);
    }

    public List<Seat> generateSeats(DateRange dateRange, int availableSeatCount) {
        List<Seat> seats = new ArrayList<>();

        List<Restaurant> restaurants = seatDataProvider.getAllRestaurants();
        List<ReservationTime> reservationTimes = seatDataProvider.getAllReservationTimes();
        List<SeatType> seatTypes = seatDataProvider.getAllSeatTypes();

        for (LocalDate reservationDate = dateRange.startDate(); reservationDate.isBefore(dateRange.endDate()); reservationDate = reservationDate.plusDays(1)) {
            for (Restaurant restaurant : restaurants) {
                for (ReservationTime reservationTime : reservationTimes) {
                    for (SeatType seatType : seatTypes) {
                        seats.add(Seat.create(restaurant, reservationDate, reservationTime, seatType, availableSeatCount));
                    }
                }
            }
        }
        return seats;
    }
}
