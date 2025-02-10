package com.trinity.ctc.domain.seat.entity;

import com.trinity.ctc.domain.reservation.entity.Reservation;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class SeatType {

    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private int minCapacity;
    private int maxCapacity;

    @OneToMany(mappedBy = "seatType")
    private List<SeatAvailability> seatAvailabilityList = new ArrayList<>();

    @OneToMany(mappedBy = "seatType")
    private List<Reservation> reservationList = new ArrayList<>();
}
