package com.trinity.ctc.util.validator;

import com.trinity.ctc.util.exception.CustomException;
import com.trinity.ctc.util.exception.error_code.TicketErrorCode;
import com.trinity.ctc.util.exception.error_code.EmptyTicketErrorCode;

public class TicketValidator {

    private TicketValidator() {}

    public static void validateTicketCount(int CurrentTicketCount, int price) {
        validateTicketCount(CurrentTicketCount);
        validateTicketPay(CurrentTicketCount, price);
    }

    private static void validateTicketCount(int CurrentTicketCount) {
        if (CurrentTicketCount < 0) {
            throw new CustomException(TicketErrorCode.NEGATIVE_TICKET_COUNT);
        }
    }

    private static void validateTicketPay(int CurrentTicketCount, int price) {
        if (CurrentTicketCount - price < 0) {
            throw new CustomException(TicketErrorCode.NOT_ENOUGH_REMAINING_TICKETS);
        }
    }

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
