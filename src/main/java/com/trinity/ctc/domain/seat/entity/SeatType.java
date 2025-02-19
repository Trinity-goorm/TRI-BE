package com.trinity.ctc.domain.seat.entity;

import com.trinity.ctc.domain.reservation.entity.Reservation;
import jakarta.persistence.*;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
public class SeatType {

    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int minCapacity;
    private int maxCapacity;

    @OneToMany(mappedBy = "seatType")
    private List<SeatAvailability> seatAvailabilityList = new ArrayList<>();

    @OneToMany(mappedBy = "seatType")
    private List<Reservation> reservationList = new ArrayList<>();
}
