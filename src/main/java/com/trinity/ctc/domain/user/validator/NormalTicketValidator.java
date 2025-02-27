package com.trinity.ctc.domain.user.validator;

import com.trinity.ctc.util.exception.CustomException;
import com.trinity.ctc.util.exception.error_code.TicketErrorCode;

public class NormalTicketValidator {


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
}
