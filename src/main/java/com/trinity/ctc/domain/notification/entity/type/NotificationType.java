package com.trinity.ctc.domain.notification.entity.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationType {
    EMPTY_NOTIFICATION(1, "신청하신 자리에 자리가 났습니다."),
    DAILY_NOTIFICATION(2, "오늘 식당 예약하셨습니다."),
    BEFORE_ONE_HOUR_NOTIFICATION(3, "한 시간 뒤, 예약있습니다.");

    private final int code;
    private final String message;
}
