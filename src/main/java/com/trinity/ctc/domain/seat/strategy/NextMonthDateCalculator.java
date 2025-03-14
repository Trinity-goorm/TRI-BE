package com.trinity.ctc.domain.seat.strategy;

import com.trinity.ctc.global.records.DateRange;

import java.time.LocalDate;

public class NextMonthDateCalculator implements DateRangeCalculator{

    @Override
    public DateRange calculateDateRange() {
        LocalDate startDate = LocalDate.now().withDayOfMonth(1).plusMonths(1); // 다음 달 1일
        LocalDate endDate = startDate.plusMonths(1); // 다음 달 말일
        return new DateRange(startDate, endDate);
    }
}
