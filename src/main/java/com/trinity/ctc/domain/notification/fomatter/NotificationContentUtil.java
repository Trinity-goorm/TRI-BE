package com.trinity.ctc.domain.notification.fomatter;

import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.LocalTime;

// FCM 알림 메세지 포맷터
public class NotificationContentUtil {
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

    public static String formatReservationNotificationUrl() {
        return "http://localhost:5173/mydining";
    }

    public static String formatSeatNotificationTitle(String restaurantName) {
        return MessageFormat.format("[캐치핑] {0}에 빈자리가 생겼어요!", restaurantName);
    }

    public static String formatSeatNotificationBody(LocalDate date, LocalTime time, int min, int max) {
        return MessageFormat.format("날짜: {0}\r\n시간: {1}\r\n인원: {2}~{3}명",
                date, time, min, max);
    }

    public static String formatSeatNotificationUrl(long restaurantId) {
        return MessageFormat.format(" http://localhost:5173/detail/{0}", restaurantId);
    }

    public static String formatReservationCompleteNotificationTitle(String restaurantName) {
        return MessageFormat.format("예약해주셔서 감사합니다.\r\n{0}에서 예약 내역을 알려드립니다.", restaurantName);
    }

    public static String formatReservationCompleteNotificationBody(LocalDate date, LocalTime time, int min, int max) {
        return MessageFormat.format("날짜: {0}\r\n시간: {1}\r\n인원: {2}~{3}명",
                date, time, min, max);
    }

    public static String formatReservationFullCanceledNotificationTitle(String restaurantName) {
        return MessageFormat.format("{0} 예약이 취소되었습니다.\r\n환불 정책에 따라 예약 티켓은 100% 환불됩니다.", restaurantName);
    }

    public static String formatReservationFullCanceledNotificationBody(LocalDate date, LocalTime time, int normalTickerCount) {
        return MessageFormat.format("날짜: {0}\r\n시간: {1}\r\n환불 티켓: 10개\r\n소유 티켓: {2}",
                date, time, normalTickerCount);
    }

    public static String formatReservationNullCanceledNotificationTitle(String restaurantName) {
        return MessageFormat.format("{0} 예약이 취소되었습니다.\r\n환불 정책에 따라 지불한 예약 티켓은 환불되지 않습니다.", restaurantName);
    }

    public static String formatReservationNullCanceledNotificationBody(LocalDate date, LocalTime time, int normalTickerCount) {
        return MessageFormat.format("날짜: {0}\r\n시간: {1}\r\n소유 티켓: {2}",
                date, time, normalTickerCount);
    }
}
