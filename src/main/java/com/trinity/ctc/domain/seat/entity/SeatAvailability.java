package com.trinity.ctc.domain.seat.entity;

import com.trinity.ctc.domain.reservation.entity.ReservationTime;
import com.trinity.ctc.domain.restaurant.entity.Restaurant;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class SeatAvailability {

    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private LocalDateTime reservationDate;
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
}
