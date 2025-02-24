package com.trinity.ctc.domain.notification.type;

import lombok.Getter;

@Getter
public enum NotificationType {
    SEAT_NOTIFICATION,
    DAILY_NOTIFICATION,
    BEFORE_ONE_HOUR_NOTIFICATION,
    RESERVATION_COMPLETE,
    RESERVATION_CANCELED
}
