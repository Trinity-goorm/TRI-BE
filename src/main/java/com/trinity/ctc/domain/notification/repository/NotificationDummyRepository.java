package com.trinity.ctc.domain.notification.repository;

import com.trinity.ctc.domain.notification.entity.ReservationNotification;
import com.trinity.ctc.domain.notification.entity.SeatNotification;
import com.trinity.ctc.domain.notification.entity.SeatNotificationSubscription;

import java.util.List;

public interface NotificationDummyRepository {
    void batchInsertReservationNotifications(List<ReservationNotification> reservationNotifications, int batchSize);

    void batchInsertSeatNotifications(List<SeatNotification> seatNotifications, int batchSize);

    void batchInsertSeatNotificationSubscriptions(List<SeatNotificationSubscription> seatNotificationSubscriptions, int batchSize);
}
