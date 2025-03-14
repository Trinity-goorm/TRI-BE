package com.trinity.ctc.domain.seat.service.provider;

import com.trinity.ctc.domain.reservation.entity.ReservationTime;
import com.trinity.ctc.domain.reservation.service.ReservationTimeService;
import com.trinity.ctc.domain.restaurant.entity.Restaurant;
import com.trinity.ctc.domain.restaurant.service.RestaurantService;
import com.trinity.ctc.domain.seat.entity.SeatType;
import com.trinity.ctc.domain.seat.service.SeatTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SeatDataProviderImpl implements SeatDataProvider {

    private final RestaurantService restaurantService;
    private final ReservationTimeService reservationTimeService;
    private final SeatTypeService seatTypeService;

    @Override
    public List<Restaurant> getAllRestaurants() {
        return restaurantService.getAllRestaurants();
    }

    @Override
    public List<ReservationTime> getAllReservationTimes() {
        return reservationTimeService.getAllReservationTimes();
    }

    @Override
    public List<SeatType> getAllSeatTypes() {
        return seatTypeService.getAllSeatTypes();
    }

}
