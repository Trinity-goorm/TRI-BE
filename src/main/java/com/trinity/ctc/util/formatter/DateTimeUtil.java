package com.trinity.ctc.util.formatter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateTimeUtil {

    // yyyy-MM-dd 포맷터
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // yyyy-MM-dd HH:mm 포맷터
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    // 1. LocalDateTime -> yyyy-MM-dd 형식의 문자열로 변환
    public static String formatToDate(LocalDateTime dateTime) {
        return dateTime.format(DATE_FORMATTER);
    }

    // 2. LocalDateTime -> yyyy-MM-dd HH:mm 형식의 문자열로 변환
    public static String formatToDateTime(LocalDateTime dateTime) {
        return dateTime.format(DATE_TIME_FORMATTER);
    }

    // 3. LocalDateTime -> yyyy-MM-dd -> 예: 2025-02-10T00:00
    public static LocalDateTime truncateToDate(LocalDateTime dateTime) {
        return dateTime.toLocalDate().atStartOfDay(); // 시간 정보를 00:00:00으로 초기화
    }

    // 4. LocalDateTime -> yyyy-MM-dd HH:mm -> 예: 2025-02-10T15:45
    public static LocalDateTime truncateToMinute(LocalDateTime dateTime) {
        return dateTime.withSecond(0).withNano(0); // 초와 나노초 제거
    }
}
