package com.trinity.ctc.domain.notification.entity;

import com.trinity.ctc.domain.seat.entity.SeatAvailability;
import jakarta.persistence.*;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
public class SeatNotificationMessage {
    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String title;
    private String body;
    private String Url;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_availability_id")
    private SeatAvailability seatAvailability;

    @OneToMany(mappedBy = "seatNotificationMessage")
    private List<SeatNotification> seatNotificationList = new ArrayList<>();
}
