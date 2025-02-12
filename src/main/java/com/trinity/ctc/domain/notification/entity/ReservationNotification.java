package com.trinity.ctc.domain.notification.entity;

import com.trinity.ctc.domain.reservation.entity.Reservation;
import com.trinity.ctc.domain.user.entity.User;
import jakarta.persistence.*;

import java.time.LocalDateTime;

public class ReservationNotification {

    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private LocalDateTime scheduledTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id")
    private Reservation reservation;
}
