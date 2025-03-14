package com.trinity.ctc.domain.seat.service.provider;

import com.trinity.ctc.domain.reservation.entity.ReservationTime;
import com.trinity.ctc.domain.restaurant.entity.Restaurant;
import com.trinity.ctc.domain.seat.entity.SeatType;

import java.util.List;

public interface SeatDataProvider {
    List<Restaurant> getAllRestaurants();
    List<ReservationTime> getAllReservationTimes();
    List<SeatType> getAllSeatTypes();
}
