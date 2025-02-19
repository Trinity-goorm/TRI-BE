package com.trinity.ctc.domain.notification.entity;

import com.trinity.ctc.domain.seat.entity.SeatAvailability;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SeatNotificationMessage {
    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String title;
    private String body;
    private String url;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_availability_id")
    private SeatAvailability seatAvailability;

    @OneToMany(mappedBy = "seatNotificationMessage")
    private List<SeatNotification> seatNotificationList = new ArrayList<>();

    @Builder
    public SeatNotificationMessage (String title, String body, String url, SeatAvailability seatAvailability) {
        this.title = title;
        this.body = body;
        this.url = url;
        this.seatAvailability = seatAvailability;
    }
}
