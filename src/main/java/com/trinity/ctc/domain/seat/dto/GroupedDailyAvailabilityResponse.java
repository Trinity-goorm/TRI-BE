package com.trinity.ctc.domain.seat.dto;

import com.trinity.ctc.util.formatter.DateTimeUtil;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

/**
 * 날짜 선택 시, 예약시간 + 시간별 좌석상태
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class GroupedDailyAvailabilityResponse {
    private final String selectedDate; // 선택된 날짜
    private final List<GroupedTimeSlotResponse> groupedTimeSlotResponse; // 타임슬롯별 예약 가능 정보

    // 정적 팩토리 메서드
    public static GroupedDailyAvailabilityResponse fromSingleTimeSlot(LocalDate selectedDate, GroupedTimeSlotResponse groupedTimeSlotResponse) {
        return new GroupedDailyAvailabilityResponse(
                DateTimeUtil.formatToDate(selectedDate),
                Collections.singletonList(groupedTimeSlotResponse) // 단일 TimeSlot을 리스트로 래핑
        );
    }

    public static GroupedDailyAvailabilityResponse fromMultipleTimeSlots(LocalDate selectedDate, List<GroupedTimeSlotResponse> groupedTimeSlotResponses) {
        return new GroupedDailyAvailabilityResponse(
                DateTimeUtil.formatToDate(selectedDate),
                groupedTimeSlotResponses
        );
    }
}
