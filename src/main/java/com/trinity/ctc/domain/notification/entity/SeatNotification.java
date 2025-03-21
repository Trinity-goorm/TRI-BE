package com.trinity.ctc.domain.notification.entity;

import com.trinity.ctc.domain.seat.entity.Seat;
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
public class SeatNotification {
    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String title;
    private String body;
    private String url;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_id")
    private Seat seat;

    @OneToMany(mappedBy = "seatNotification", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SeatNotificationSubscription> seatNotificationSubscriptionList = new ArrayList<>();

    @Builder
    public SeatNotification(String title, String body, String url, Seat seat) {
        this.title = title;
        this.body = body;
        this.url = url;
        this.seat = seat;
    }
}
