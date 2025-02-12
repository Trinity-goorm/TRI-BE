package com.trinity.ctc.domain.notification.entity;

import com.trinity.ctc.domain.seat.entity.SeatAvailability;
import com.trinity.ctc.domain.user.entity.User;
import jakarta.persistence.*;

@Entity
public class AvailableSeatNotification {

    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_availability_id")
    private SeatAvailability seatAvailability;
}
