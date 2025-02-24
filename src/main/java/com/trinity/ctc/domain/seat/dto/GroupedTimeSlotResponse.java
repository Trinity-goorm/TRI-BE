package com.trinity.ctc.domain.seat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * 시간 별 좌석 상세
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Schema(description = "타임슬롯별 예약 가능 정보")
public class GroupedTimeSlotResponse {

    @Schema(description = "타임슬롯 (예: 09:00)", example = "09:00")
    private final String timeSlot;

    @Schema(description = "해당 타임슬롯의 예약 가능 여부", example = "true")
    private final boolean isAvailable;

    @Schema(description = "좌석별 예약가능 정보", implementation = GroupedSeatResponse.class)
    private final List<GroupedSeatResponse> groupedSeats;

    public static GroupedTimeSlotResponse fromGroupedSeats(String timeSlot, boolean isAvailable, List<GroupedSeatResponse> seats ) {
        return new GroupedTimeSlotResponse(
                timeSlot, isAvailable, seats
        );
    }
}
