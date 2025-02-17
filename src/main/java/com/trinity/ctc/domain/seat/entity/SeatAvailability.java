package com.trinity.ctc.domain.seat.entity;

import com.trinity.ctc.domain.reservation.entity.ReservationTime;
import com.trinity.ctc.domain.restaurant.entity.Restaurant;
import com.trinity.ctc.util.validator.CapacityValidator;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Entity
public class SeatAvailability {

    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private LocalDate reservationDate;
    private int availableSeats;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id")
    private Restaurant restaurant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_time_id")
    private ReservationTime reservationTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_type_id")
    private SeatType seatType;

    /* 내부 로직 */
    public void preoccupyOneSeat() {
        CapacityValidator.validateAvailableSeats(this.availableSeats);
        availableSeats--;
    }
}
