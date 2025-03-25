package com.trinity.ctc.domain.notification.validator;

import com.trinity.ctc.global.exception.CustomException;
import com.trinity.ctc.global.exception.error_code.EmptyTicketErrorCode;

public class EmptyTicketValidator {
    // 빈자리 알림 신청 시, 사용 티켓 수(global constant로 처리해야 함)
    private static final int USED_EMPTY_TICKET = 1;

    private EmptyTicketValidator() {
    }

    // 빈자리 티켓 사용 시, 현재 티켓 수와 사용 시, 티켓 수 검증
    public static void validateEmptyTicketUsage(int CurrentEmptyTicketCount) {
        validateEmptyTicketCount(CurrentEmptyTicketCount);
        if (CurrentEmptyTicketCount - USED_EMPTY_TICKET < 0) {
            throw new CustomException(EmptyTicketErrorCode.NOT_ENOUGH_REMAINING_EMPTY_TICKETS);
        }
    }

    // 현재 보유한 빈자리 티켓이 0개 미만인지 검증
    private static void validateEmptyTicketCount(int CurrentEmptyTicketCount) {
        if (CurrentEmptyTicketCount < 0) {
            throw new CustomException(EmptyTicketErrorCode.NEGATIVE_EMPTY_TICKET_COUNT);
        }
    }
}
