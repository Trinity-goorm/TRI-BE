package com.trinity.ctc.domain.seat.strategy;

import com.trinity.ctc.domain.seat.mode.DateRangeMode;
import org.springframework.stereotype.Component;

@Component
public class DateRangeCalculatorFactory {
    public DateRangeCalculator getCalculator(DateRangeMode mode) {
        return switch (mode) {
            case NEXT_MONTH -> new NextMonthDateCalculator();
            case TWO_MONTHS -> new TwoMonthDateCalculator();
        };
    }
}
