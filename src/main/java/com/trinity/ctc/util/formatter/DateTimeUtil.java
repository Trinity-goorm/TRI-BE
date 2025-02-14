package com.trinity.ctc.util.formatter;

import com.trinity.ctc.util.validator.DateTimeValidator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class DateTimeUtil {

    // yyyy-MM-dd 포맷터
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // yyyy-MM-dd HH:mm 포맷터
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    // "HH:mm" 포맷터
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    // LocalDateTime -> yyyy-MM-dd 형식의 문자열로 변환
    public static String formatToDate(LocalDateTime dateTime) {
        return dateTime.format(DATE_FORMATTER);
    }

    public static String formatToDate(LocalDate date) {
        return date.format(DATE_FORMATTER);
    }

    // LocalDateTime -> yyyy-MM-dd HH:mm 형식의 문자열로 변환
    public static String formatToDateTime(LocalDateTime dateTime) {
        return dateTime.format(DATE_TIME_FORMATTER);
    }

    // LocalDateTime -> yyyy-MM-dd -> 예: 2025-02-10T00:00
    public static LocalDateTime truncateToDate(LocalDateTime dateTime) {
        return dateTime.toLocalDate().atStartOfDay(); // 시간 정보를 00:00:00으로 초기화
    }

    // LocalDateTime -> yyyy-MM-dd HH:mm -> 예: 2025-02-10T15:45
    public static LocalDateTime truncateToMinute(LocalDateTime dateTime) {
        return dateTime.withSecond(0).withNano(0); // 초와 나노초 제거
    }

    // LocalTime과 오늘 날짜를 결합하여 LocalDateTime 반환
    public static LocalDateTime combineWithToday(LocalTime timeSlot) {
        // 현재 날짜와 timeSlot을 결합
        return LocalDate.now().atTime(timeSlot);
    }

    // LocalTime과 특정 날짜를 결합하여 LocalDateTime 반환
    public static LocalDateTime combineWithDate(LocalDate date, LocalTime timeSlot) {
        // 주어진 날짜와 timeSlot을 결합
        return date.atTime(timeSlot);
    }

    // 현재 시간에서 한 시간 뒤의 LocalTime 반환
    public static LocalTime getOneHourLater(LocalDateTime dateTime) {
        LocalDateTime oneHourLater = dateTime.plusHours(1);
        return oneHourLater.toLocalTime();
    }

    // LocalDate -> LocalDateTime 컨버트
    public static LocalDateTime convertToLocalDateTime(LocalDate localDate) {
        if (DateTimeValidator.isToday(localDate)) {
            return LocalDateTime.of(LocalDate.now(), LocalTime.now());
        } else {
            return LocalDateTime.of(localDate, LocalTime.MIN);
        }
    }

    // 초 제거
    public static String formatToHHmm(LocalTime time) {
        return time.format(TIME_FORMATTER);
    }
}
