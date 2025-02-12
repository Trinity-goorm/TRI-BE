package com.trinity.ctc.domain.reservation.entity;

import com.trinity.ctc.domain.seat.entity.SeatAvailability;
import jakarta.persistence.*;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@EntityListeners(AuditingEntityListener.class)
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
    private List<SeatAvailability> seatAvailabilityList = new ArrayList<>();

}
