package com.trinity.ctc.domain.reservation.entity;

import com.trinity.ctc.domain.seat.entity.Seat;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@EntityListeners(AuditingEntityListener.class)
@RequiredArgsConstructor
public class ReservationTime {

    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private LocalTime timeSlot;

    @CreatedDate
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "reservationTime")
    private List<Reservation> reservations = new ArrayList<>();

    @OneToMany(mappedBy = "reservationTime")
    private List<Seat> seatList = new ArrayList<>();

    @Builder
    public ReservationTime(LocalTime timeSlot, LocalDateTime createdAt) {
        this.timeSlot = timeSlot;
        this.createdAt = createdAt;
    }
}
