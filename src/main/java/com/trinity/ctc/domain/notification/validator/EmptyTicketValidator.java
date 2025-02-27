package com.trinity.ctc.domain.notification.validator;

import com.trinity.ctc.global.exception.CustomException;
import com.trinity.ctc.global.exception.error_code.EmptyTicketErrorCode;

public class EmptyTicketValidator {
    private EmptyTicketValidator() {}

    // 빈자리 티켓 사용 시, 현재 티켓 수와 사용 시, 티켓 수 검증(사용 티켓 수(1)를 CONSTANT 처리해야 할 듯)
    public static void validateEmptyTicketUsage(int CurrentEmptyTicketCount) {
        validateEmptyTicketCount(CurrentEmptyTicketCount);
        if (CurrentEmptyTicketCount - 1 < 0) {
            throw new CustomException(EmptyTicketErrorCode.NOT_ENOUGH_REMAINING_EMPTY_TICKETS);
        }
    }

    private static void validateEmptyTicketCount(int CurrentEmptyTicketCount) {
        if (CurrentEmptyTicketCount < 0) {
            throw new CustomException(EmptyTicketErrorCode.NEGATIVE_EMPTY_TICKET_COUNT);
        }
    }
}
