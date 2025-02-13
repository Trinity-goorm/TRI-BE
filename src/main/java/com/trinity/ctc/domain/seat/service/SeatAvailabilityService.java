package com.trinity.ctc.domain.seat.service;

import com.trinity.ctc.domain.seat.dto.GroupedSeatResponse;
import com.trinity.ctc.domain.seat.dto.GroupedTimeSlotResponse;
import com.trinity.ctc.domain.seat.entity.SeatAvailability;
import com.trinity.ctc.domain.seat.repository.SeatAvailabilityRepository;
import com.trinity.ctc.util.formatter.DateTimeUtil;
import com.trinity.ctc.util.validator.DateTimeValidator;
import com.trinity.ctc.domain.seat.dto.GroupedDailyAvailabilityResponse;
import com.trinity.ctc.util.validator.SeatAvailabilityValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.trinity.ctc.util.validator.DateTimeValidator.isToday;

@Service
@RequiredArgsConstructor
@Slf4j
public class SeatAvailabilityService {
    private final SeatAvailabilityRepository seatAvailabilityRepository;

    /**
     * 오늘 예약가능 시간 및 좌석 별 남은 좌석 수 반환 (현재시간 한시간 뒤부터)
     * @param restaurantId
     * @param selectedDate
     * @return 좌석정보리스트와 예약가능여부 리스트
     */
    public GroupedDailyAvailabilityResponse getAvailableSeatsDay(Long restaurantId, LocalDate selectedDate) {
        // 날짜 검증 (과거인가?)
        DateTimeValidator.validate(selectedDate);

        List<SeatAvailability> availableSeatList = seatAvailabilityRepository.findAvailableSeatsForDate(restaurantId, selectedDate);
        log.info("[SeatAvailabilityService] 생성 Response 수 : {}", availableSeatList.size());

        // 현재 날짜와 검증
        boolean isToday = isToday(selectedDate);
        log.info("[SeatAvailabilityService] 오늘인가? : {}", isToday);

        // 타임슬롯으로 그룹화 -> 순서가 보장되어 나가지 않음.
        Map<LocalTime, List<SeatAvailability>> groupedByTimeslot = availableSeatList.stream()
            .collect(Collectors.groupingBy(sa -> sa.getReservationTime().getTimeSlot()));

        // 군집별 예약 가능 여부 판단 및 DTO 변환
        List<GroupedTimeSlotResponse> groupedTimeSlotResponses = groupedByTimeslot.entrySet().stream()
                .map(entry -> {
                    LocalTime timeslot = entry.getKey();
                    List<SeatAvailability> seatAvailabilities = entry.getValue();

                    // 예약 가능 여부 판단
                    boolean isAvailable = seatAvailabilities.stream()
                            .anyMatch(sa -> SeatAvailabilityValidator.validate(sa, isToday));

                    List<GroupedSeatResponse> groupedSeatResponses = seatAvailabilities.stream()
                            .map(GroupedSeatResponse::of)
                            .toList();

                    return GroupedTimeSlotResponse.fromGroupedSeats(DateTimeUtil.formatToHHmm(timeslot), isAvailable, groupedSeatResponses);
                })
                .collect(Collectors.toList());

        // 최종 응답 DTO 생성
        return GroupedDailyAvailabilityResponse.fromMultipleTimeSlots(selectedDate, groupedTimeSlotResponses);
    }
}
