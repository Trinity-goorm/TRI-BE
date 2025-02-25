package com.trinity.ctc.domain.seat.entity;

import com.trinity.ctc.domain.reservation.entity.Reservation;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@NoArgsConstructor
public class SeatType {

    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int minCapacity;
    private int maxCapacity;

    @OneToMany(mappedBy = "seatType")
    private List<Seat> seatList = new ArrayList<>();

    @OneToMany(mappedBy = "seatType")
    private List<Reservation> reservationList = new ArrayList<>();

    @Builder
    public SeatType(int minCapacity, int maxCapacity) {
        this.minCapacity = minCapacity;
        this.maxCapacity = maxCapacity;
    }
}
