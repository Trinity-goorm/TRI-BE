package com.trinity.ctc.domain.seat.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * 시간 별 좌석 상세
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class GroupedTimeSlotResponse {
    private final String timeSlot; // 타임슬롯 (예: "09:00")
    private final boolean isAvailable; // 해당 타임슬롯의 예약 가능 여부
    private final List<GroupedSeatResponse> groupedSeats; // 좌석별 예약가능 정보

    public static GroupedTimeSlotResponse fromGroupedSeats(String timeSlot, boolean isAvailable, List<GroupedSeatResponse> seats ) {
        return new GroupedTimeSlotResponse(
                timeSlot, isAvailable, seats
        );
    }
}
