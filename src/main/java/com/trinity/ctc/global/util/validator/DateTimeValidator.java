package com.trinity.ctc.global.util.validator;

import com.trinity.ctc.global.exception.error_code.DateTimeErrorCode;
import com.trinity.ctc.global.exception.CustomException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class DateTimeValidator {
    private DateTimeValidator() {
    }

    /**
     * 오늘인지 확인
     *
     * @param localDate
     * @return 오늘인지 아닌지
     */
    public static boolean isToday(LocalDate localDate) {
        LocalDate today = LocalDate.now();
        return localDate.equals(today);
    }

    /**
     * 과거인지 확인
     *
     * @param localDate
     */
    public static void isPast(LocalDate localDate) {
        LocalDate today = LocalDate.now();
        if (localDate.isBefore(today)) {
            throw new CustomException(DateTimeErrorCode.GIVEN_DATETIME_IS_PAST);
        }
    }

    /**
     * 날짜 기준으로 이틀포함 이전인지 확인
     *
     * @param localDate
     * @return 이틀 전이면 true, 아니면 false
     */
    public static boolean isMoreThanOneDayAway(LocalDate localDate) {
        LocalDate oneDayAfter = LocalDate.now().plusDays(1);
        return localDate.isAfter(oneDayAfter) | localDate.isEqual(oneDayAfter);
    }

    public static boolean isExpired(LocalDate localDate, LocalTime localTime) {
        LocalDateTime localDateTime = LocalDateTime.of(localDate, localTime);
        LocalDateTime now = LocalDateTime.now();
        return localDateTime.isBefore(now);
    }
}
