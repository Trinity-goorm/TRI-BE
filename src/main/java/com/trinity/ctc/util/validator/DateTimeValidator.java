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

    /**
     * 날짜 기준으로 이틀포함 이전인지 확인
     * @param localDate
     * @return 이틀 전이면 true, 아니면 false
     */
    public static boolean isTwoDaysAgoOrBefore(LocalDate localDate) {
        LocalDate twoDaysAgo = LocalDate.now().minusDays(2);
        return localDate.isBefore(twoDaysAgo) || localDate.isEqual(twoDaysAgo);
    }
}
