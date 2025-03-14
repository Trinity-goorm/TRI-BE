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
}
