package com.trinity.ctc.util;

import com.trinity.ctc.global.util.formatter.DateTimeUtil;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static com.trinity.ctc.global.util.validator.DateTimeValidator.isMoreThanOneDayAway;
import static org.junit.jupiter.api.Assertions.*;

public class DateTimeUtilTest {

    @Test
    public void testFormatToDate() {
        LocalDateTime dateTime = LocalDateTime.of(2025, 2, 10, 15, 45);
        String formattedDate = DateTimeUtil.formatToDate(dateTime);
        assertEquals("2025-02-10", formattedDate);
    }

    @Test
    public void testFormatToDateTime() {
        LocalDateTime dateTime = LocalDateTime.of(2025, 2, 10, 15, 45);
        String formattedDateTime = DateTimeUtil.formatToDateTime(dateTime);
        assertEquals("2025-02-10 15:45", formattedDateTime);
    }

    @Test
    public void testTruncateToDate() {
        LocalDateTime dateTime = LocalDateTime.of(2025, 2, 10, 15, 45, 30);
        LocalDateTime truncatedDate = DateTimeUtil.truncateToDate(dateTime);
        assertEquals(LocalDateTime.of(2025, 2, 10, 0, 0), truncatedDate);
    }

    @Test
    public void testTruncateToMinute() {
        LocalDateTime dateTime = LocalDateTime.of(2025, 2, 10, 15, 45, 30);
        LocalDateTime truncatedMinute = DateTimeUtil.truncateToMinute(dateTime);
        assertEquals(LocalDateTime.of(2025, 2, 10, 15, 45), truncatedMinute);
    }

    @Test
    void testIsMoreThanOneDaysAway() {
        /* 테스트 당시 날짜 = 2025-02-22 */

        // 테스트 대상 날짜들
        LocalDate reservationDate6 = LocalDate.of(2025, 2, 21);
        LocalDate reservationDate1 = LocalDate.of(2025, 2, 22);
        LocalDate reservationDate2 = LocalDate.of(2025, 2, 23);
        LocalDate reservationDate3 = LocalDate.of(2025, 2, 24);
        LocalDate reservationDate4 = LocalDate.of(2025, 2, 25);
        LocalDate reservationDate5 = LocalDate.of(2025, 2, 26);

        // 테스트 실행
        assertFalse(isMoreThanOneDayAway(reservationDate6));
        assertFalse(isMoreThanOneDayAway(reservationDate1));
        assertTrue(isMoreThanOneDayAway(reservationDate2));
        assertTrue(isMoreThanOneDayAway(reservationDate3));
        assertTrue(isMoreThanOneDayAway(reservationDate4));
        assertTrue(isMoreThanOneDayAway(reservationDate5));
    }
}
