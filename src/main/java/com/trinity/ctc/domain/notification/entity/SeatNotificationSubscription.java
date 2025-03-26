package com.trinity.ctc.domain.notification.entity;

import com.trinity.ctc.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SeatNotificationSubscription {

    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_notification_id")
    private SeatNotification seatNotification;

    @Builder
    public SeatNotificationSubscription(User user, SeatNotification seatNotification) {
        this.user = user;
        this.seatNotification = seatNotification;
    }
    
    public static SeatNotificationSubscription createSeatNotificationSubscription(User user, SeatNotification seatNotification) {
        return SeatNotificationSubscription.builder()
                .user(user)
                .seatNotification(seatNotification)
                .build();
    }
}