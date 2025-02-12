package com.trinity.ctc.util.validator;

import com.trinity.ctc.util.exception.CustomException;
import com.trinity.ctc.util.exception.error_code.DateTimeErrorCode;

import java.time.LocalDate;

public class DateTimeValidator {
    private DateTimeValidator() {}

    /**
     * 오늘인지 확인
     * @param localDate
     * @return 오늘인지 아닌지
     */
    public static boolean isToday(LocalDate localDate) {
        LocalDate today = LocalDate.now();
        return localDate.equals(today);
    }

    /**
     * 과거인지 확인
     * @param localDate
     */
    public static void validate(LocalDate localDate) {
        LocalDate today = LocalDate.now();
        if (localDate.isBefore(today)) {
            throw new CustomException(DateTimeErrorCode.GIVEN_DATETIME_IS_PAST);
        }
    }
}
