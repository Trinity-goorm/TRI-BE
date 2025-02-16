package com.trinity.ctc.domain.notification.entity;

import com.trinity.ctc.domain.user.entity.User;
import jakarta.persistence.*;

@Entity
public class SeatNotification {

    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_notification_message_id")
    private SeatNotificationMessage seatNotificationMessage;
}