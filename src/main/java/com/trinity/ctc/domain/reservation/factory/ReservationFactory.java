package com.trinity.ctc.domain.reservation.factory;

import com.trinity.ctc.domain.reservation.entity.Reservation;
import com.trinity.ctc.domain.reservation.repository.ReservationTimeRepository;
import com.trinity.ctc.domain.reservation.status.ReservationStatus;
import com.trinity.ctc.domain.restaurant.repository.RestaurantRepository;
import com.trinity.ctc.domain.seat.repository.SeatTypeRepository;
import com.trinity.ctc.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ReservationFactory {

    private final RestaurantRepository restaurantRepository;
    private final UserRepository userRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final SeatTypeRepository seatTypeRepository;

    public List<Reservation> createReservationsByCsv(List<Map<String, String>> csvReservationData) {
        List<Reservation> reservations = new ArrayList<>();
        for (Map<String, String> row : csvReservationData) {
            Reservation reservation = Reservation.builder()
                    .reservationDate(LocalDate.parse(row.get("reservation_date")))
                    .status(ReservationStatus.valueOf(row.get("status")))
                    .restaurant(restaurantRepository.findById(Long.parseLong(row.get("restaurant_id"))).orElse(null))
                    .user(userRepository.findById(Long.parseLong(row.get("user_id"))).orElse(null))
                    .reservationTime(reservationTimeRepository.findById(Long.parseLong(row.get("reservation_time_id"))).orElse(null))
                    .seatType(seatTypeRepository.findById(Long.parseLong(row.get("seat_type_id"))).orElse(null))
                    .build();

            reservations.add(reservation);
        }
        return reservations;
    }
}
