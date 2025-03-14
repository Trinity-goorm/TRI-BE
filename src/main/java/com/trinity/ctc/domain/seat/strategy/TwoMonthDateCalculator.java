package com.trinity.ctc.domain.seat.strategy;

import com.trinity.ctc.global.records.DateRange;

import java.time.LocalDate;

public class TwoMonthDateCalculator implements DateRangeCalculator{

    @Override
    public DateRange calculateDateRange() {
        LocalDate startDate = LocalDate.now().withDayOfMonth(1); // 이번 달 1일
        LocalDate endDate = startDate.plusMonths(2); // 2개월 후 말일
        return new DateRange(startDate, endDate);
    }
}
