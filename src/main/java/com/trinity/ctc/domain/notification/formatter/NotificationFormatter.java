package com.trinity.ctc.domain.notification.formatter;

import com.trinity.ctc.domain.notification.entity.ReservationNotification;
import com.trinity.ctc.domain.notification.entity.SeatNotification;
import com.trinity.ctc.domain.reservation.entity.Reservation;
import com.trinity.ctc.domain.seat.entity.Seat;
import com.trinity.ctc.domain.user.entity.User;
import com.trinity.ctc.global.util.formatter.DateTimeUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static com.trinity.ctc.domain.notification.entity.ReservationNotification.createReservationNotification;
import static com.trinity.ctc.domain.notification.entity.SeatNotification.createSeatNotification;
import static com.trinity.ctc.domain.notification.type.NotificationType.BEFORE_ONE_HOUR_NOTIFICATION;
import static com.trinity.ctc.domain.notification.type.NotificationType.DAILY_NOTIFICATION;
import static com.trinity.ctc.global.util.formatter.DateTimeUtil.combineWithDate;

// 각 알림 entity 들을 반환하는 로직을 구현한 factory class
public class NotificationFormatter {

    // 빈자리 알림 formatter
    public static SeatNotification formattingSeatNotification(Seat seat) {
        // 빈자리 알림 메세지에 필요한 정보 변수 선언
        LocalDate date = seat.getReservationDate();
        LocalTime time = seat.getReservationTime().getTimeSlot();
        long restaurantId = seat.getRestaurant().getId();
        String restaurantName = seat.getRestaurant().getName();
        int minCapacity = seat.getSeatType().getMinCapacity();
        int maxCapacity = seat.getSeatType().getMaxCapacity();

        // 알림 메세지 data 별 포멧팅
        String title = NotificationContentFormatter.formatSeatNotificationTitle(restaurantName);
        String body = NotificationContentFormatter.formatSeatNotificationBody(date, time, minCapacity, maxCapacity);
        String url = NotificationContentFormatter.formatSeatNotificationUrl(restaurantId);

        // seatNotification entity 를 생성하는 팩토리 메서드 호출 -> seatNotification 반환
        return createSeatNotification(title, body, url, seat);
    }

    // 당일 예약 알림 formatter
    public static ReservationNotification formattingDailyNotification(User user, Reservation reservation) {
        // 당일 알림 메세지에 필요한 정보 변수 선언
        String userName = user.getNickname();
        String restaurantName = reservation.getRestaurant().getName();
        LocalDate reservedDate = reservation.getReservationDate();
        LocalTime reservedTime = reservation.getReservationTime().getTimeSlot();
        LocalDateTime scheduledTime = combineWithDate(reservedDate, LocalTime.of(8, 0));

        // 알림 메세지 data 별 포멧팅
        String title = NotificationContentFormatter.formatDailyNotificationTitle(userName, restaurantName);
        String body = NotificationContentFormatter.formatDailyNotificationBody(restaurantName, reservedDate, reservedTime);
        String url = NotificationContentFormatter.formatReservationNotificationUrl();

        // reservationNotification entity 를 생성하는 팩토리 메서드 호출 -> reservationNotification 반환
        return createReservationNotification(DAILY_NOTIFICATION, user, reservation, title, body, url, scheduledTime);
    }

    // 1시간 전 예약 알림 formatter
    public static ReservationNotification formattingHourBeforeNotification(User user, Reservation reservation) {
        // 당일 알림 메세지에 필요한 정보 변수 선언
        String userName = user.getNickname();
        String restaurantName = reservation.getRestaurant().getName();
        LocalDate reservedDate = reservation.getReservationDate();
        LocalTime reservedTime = reservation.getReservationTime().getTimeSlot();
        LocalDateTime scheduledTime = DateTimeUtil.combineWithDate(reservedDate, reservedTime).minusHours(1);

        // 알림 메세지 data 별 포멧팅
        String title = NotificationContentFormatter.formatHourBeforeNotificationTitle(userName, restaurantName);
        String body = NotificationContentFormatter.formatHourBeforeNotificationBody(restaurantName, reservedDate, reservedTime);
        String url = NotificationContentFormatter.formatReservationNotificationUrl();

        // reservationNotification entity 를 생성하는 팩토리 메서드 호출 -> reservationNotification 반환
        return createReservationNotification(BEFORE_ONE_HOUR_NOTIFICATION, user, reservation, title, body, url, scheduledTime);
    }
}
