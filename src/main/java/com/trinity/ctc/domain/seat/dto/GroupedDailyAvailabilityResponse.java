package com.trinity.ctc.domain.seat.dto;

import com.trinity.ctc.global.util.formatter.DateTimeUtil;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "일별 예약가능 응답")
public class GroupedDailyAvailabilityResponse {

    @Schema(description = "선택된 날짜", example = "2025-02-13")
    private final String selectedDate;

    @Schema(description = "타임슬롯별 예약 가능 정보", implementation = GroupedTimeSlotResponse.class)
    private final List<GroupedTimeSlotResponse> groupedTimeSlotResponse;

    public static GroupedDailyAvailabilityResponse fromSingleTimeSlot(LocalDate selectedDate, GroupedTimeSlotResponse groupedTimeSlotResponse) {
        return new GroupedDailyAvailabilityResponse(
                DateTimeUtil.formatToDate(selectedDate),
                Collections.singletonList(groupedTimeSlotResponse)
        );
    }

    public static GroupedDailyAvailabilityResponse fromMultipleTimeSlots(LocalDate selectedDate, List<GroupedTimeSlotResponse> groupedTimeSlotResponses) {
        return new GroupedDailyAvailabilityResponse(
                DateTimeUtil.formatToDate(selectedDate),
                groupedTimeSlotResponses
        );
    }
}
