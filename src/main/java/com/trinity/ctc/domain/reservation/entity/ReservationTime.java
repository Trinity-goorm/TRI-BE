package com.trinity.ctc.domain.reservation.entity;

import com.trinity.ctc.domain.seat.entity.SeatAvailability;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
public class ReservationTime {

    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private LocalDateTime timeSlot;
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "reservationTime")
    private List<Reservation> reservations = new ArrayList<>();

    @OneToMany(mappedBy = "reservationTime")
    private List<SeatAvailability> seatAvailabilityList = new ArrayList<>();

}
