package com.trinity.ctc.domain.seat.service;

import com.trinity.ctc.domain.reservation.entity.ReservationTime;
import com.trinity.ctc.domain.reservation.service.ReservationTimeService;
import com.trinity.ctc.domain.restaurant.entity.Restaurant;
import com.trinity.ctc.domain.restaurant.service.RestaurantService;
import com.trinity.ctc.domain.seat.entity.Seat;
import com.trinity.ctc.domain.seat.entity.SeatType;
import com.trinity.ctc.domain.seat.mode.DateRangeMode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SeatPreparationService {

    private final RestaurantService restaurantService;
    private final ReservationTimeService reservationTimeService;
    private final SeatTypeService seatTypeService;

    public List<Seat> prepareSeatsData(DateRangeMode mode, int availableSeatCount) {
        List<Restaurant> restaurants = restaurantService.getAllRestaurants();
        List<ReservationTime> reservationTimes = reservationTimeService.getAllReservationTimes();
        List<SeatType> seatTypes = seatTypeService.getAllSeatTypes();

        LocalDate startDate;
        LocalDate endDate;

        if (mode == DateRangeMode.NEXT_MONTH) {
            startDate = LocalDate.now().withDayOfMonth(1).plusMonths(1); // 다음 달 1일
            endDate = startDate.plusMonths(1); // 다음 달 말일까지
        } else {
            startDate = LocalDate.now().withDayOfMonth(1); // 이번 달 1일
            endDate = startDate.plusMonths(2); // 다음 달 말일까지
        }

        List<Seat> seats = new ArrayList<>();

        for (LocalDate reservationDate = startDate; reservationDate.isBefore(endDate); reservationDate = reservationDate.plusDays(1)) {
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
