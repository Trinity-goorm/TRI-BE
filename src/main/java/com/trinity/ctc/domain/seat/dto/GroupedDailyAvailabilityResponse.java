package com.trinity.ctc.domain.seat.dto;

import com.trinity.ctc.util.formatter.DateTimeUtil;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class GroupedDailyAvailabilityResponse {
    private final String selectedDate; // 선택된 날짜
    private final List<TimeSlotAvailability> timeSlotAvailabilities; // 타임슬롯별 예약 가능 정보

    @Getter
    @AllArgsConstructor
    public static class TimeSlotAvailability {
        private final String timeSlot; // 타임슬롯 (예: "09:00")
        private final boolean isAvailable; // 해당 타임슬롯의 예약 가능 여부
    }

    // 정적 팩토리 메서드
    public static GroupedDailyAvailabilityResponse fromSingleTimeSlot(LocalDate selectedDate, TimeSlotAvailability timeSlotAvailability) {
        return new GroupedDailyAvailabilityResponse(
                DateTimeUtil.formatToDate(selectedDate),
                Collections.singletonList(timeSlotAvailability) // 단일 TimeSlot을 리스트로 래핑
        );
    }

    public static GroupedDailyAvailabilityResponse fromMultipleTimeSlots(LocalDate selectedDate, List<TimeSlotAvailability> timeSlotAvailabilities) {
        return new GroupedDailyAvailabilityResponse(
                DateTimeUtil.formatToDate(selectedDate),
                timeSlotAvailabilities
        );
    }
}
