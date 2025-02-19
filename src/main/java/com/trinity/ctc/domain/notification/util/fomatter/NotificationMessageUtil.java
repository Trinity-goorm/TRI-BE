package com.trinity.ctc.domain.notification.util.fomatter;

import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.LocalTime;

// FCM 알림 메세지 포맷터
public class NotificationMessageUtil {
    public static String formatDailyNotificationTitle(String userName, String restaurantName) {
        return MessageFormat.format("[캐치핑] {0}님, 오늘 {1}에 예약이 있어요!",
                                    userName, restaurantName);
    }

    public static String formatDailyNotificationBody(String restaurantName, LocalDate date, LocalTime time) {
        return MessageFormat.format("식당: {0}\r\n날짜: {1}\r\n시간: {2}",
                                    restaurantName, date, time);
    }

    public static String formatHourBeforeNotificationTitle(String userName, String restaurantName) {
        return MessageFormat.format("[캐치핑] {0}님, 1시간 후 {1}에서의 맛있는 식사가 기다리고 있어요!",
                                    userName, restaurantName);
    }

    public static String formatHourBeforeNotificationBody(String restaurantName, LocalDate date, LocalTime time) {
        return MessageFormat.format("식당: {0}\r\n날짜: {1}\r\n시간: {2}",
                                    restaurantName, date, time);
    }

    public static String formatReservationNotificationUrl(long reservationId) {
        return MessageFormat.format("http://localhost:5173/reservation/{0}", reservationId);
    }

    public static String formatSeatNotificationTitle(String userName, String restaurantName) {
        return MessageFormat.format("[캐치핑] {0}에 빈자리가 생겼어요!", restaurantName);
    }

    public static String formatSeatNotificationBody(LocalDate date, LocalTime time, int min, int max) {
        return MessageFormat.format("날짜: {0}\r\n시간: {1}\r\n인원: {2}~{3}명",
                                    date, time, min, max);
    }

    public static String formatSeatNotificationUrl(long restaurantId) {
        return MessageFormat.format("http://localhost:5173/reservation/{0}", restaurantId);
    }
}
