package com.trinity.ctc.domain.seat.service;

import com.trinity.ctc.domain.reservation.dto.ReservationAvailabilityResponse;
import com.trinity.ctc.domain.seat.dto.GroupedDailyAvailabilityResponse;
import com.trinity.ctc.domain.seat.dto.GroupedSeatResponse;
import com.trinity.ctc.domain.seat.dto.GroupedTimeSlotResponse;
import com.trinity.ctc.domain.seat.entity.SeatAvailability;
import com.trinity.ctc.domain.seat.repository.SeatAvailabilityRepository;
import com.trinity.ctc.util.formatter.DateTimeUtil;
import com.trinity.ctc.util.helper.GroupingHelper;
import com.trinity.ctc.util.validator.DateTimeValidator;
import com.trinity.ctc.util.validator.SeatAvailabilityValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
    @Transactional(readOnly = true)
    public GroupedDailyAvailabilityResponse getAvailableSeatsDay(Long restaurantId, LocalDate selectedDate) {

        DateTimeValidator.isPast(selectedDate);
        List<SeatAvailability> availableSeatList = fetchAvailableSeats(restaurantId, selectedDate);

        boolean isToday = isToday(selectedDate);

        Map<LocalTime, List<SeatAvailability>> groupedByTimeslot = GroupingHelper.groupByTimeSlot(availableSeatList);
        List<GroupedTimeSlotResponse> groupedTimeSlotResponses = createGroupedTimeSlotResponses(groupedByTimeslot, isToday);

        return GroupedDailyAvailabilityResponse.fromMultipleTimeSlots(selectedDate, groupedTimeSlotResponses);
    }

    /**
     * 14일간의 날짜별 예약 가능 여부 반환
     * @param restaurantId
     * @return 날짜별 예약 가능 여부 리스트 (ReservationAvailabilityDto 형태)
     */
    @Transactional(readOnly = true)
    public List<ReservationAvailabilityResponse> getAvailabilityForNext14Days(Long restaurantId) {
        LocalDate today = LocalDate.now();

        return IntStream.range(0, 14)
            .mapToObj(i -> {
                LocalDate targetDate = today.plusDays(i);
                List<SeatAvailability> availableSeatList= fetchAvailableSeats(restaurantId, targetDate);
                // 예약 가능 여부 판단
                boolean isAvailable = SeatAvailabilityValidator.isAnySeatAvailable(availableSeatList, isToday(targetDate));
                return new ReservationAvailabilityResponse(targetDate, isAvailable);
            })
            .collect(Collectors.toList());
    }

    /* 내부 메서드 */
    /**
     * 특정 식당, 날짜의 예약가능데이터 획득
     * @param restaurantId
     * @param selectedDate
     * @return 특정 식당, 날짜의 예약가능데이터
     */
    private List<SeatAvailability> fetchAvailableSeats(Long restaurantId, LocalDate selectedDate) {
        List<SeatAvailability> availableSeatList = seatAvailabilityRepository.findAvailableSeatsForDate(restaurantId, selectedDate);
        log.info("[SeatAvailabilityService] 생성 Response 수 : {}", availableSeatList.size());
        return availableSeatList;
    }

    /**
     * 예약시갅으로 그룹화 -> 에약시간 별 좌석타입들
     * @param groupedByTimeslot
     * @param isToday
     * @return
     */
    private List<GroupedTimeSlotResponse> createGroupedTimeSlotResponses(Map<LocalTime, List<SeatAvailability>> groupedByTimeslot, boolean isToday) {
        return groupedByTimeslot.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> createGroupedTimeSlotResponse(entry.getKey(), entry.getValue(), isToday))
                .collect(Collectors.toList());
    }

    /**
     * 그룹화된 타임슬롯 응답 생성
     * @param timeslot
     * @param seatAvailabilities
     * @param isToday
     * @return 타임슬롯으로 그룹화 된 응답
     */
    private GroupedTimeSlotResponse createGroupedTimeSlotResponse(LocalTime timeslot, List<SeatAvailability> seatAvailabilities, boolean isToday) {
        // 예약 가능 여부 판단
        boolean isAvailable = SeatAvailabilityValidator.isAnySeatAvailable(seatAvailabilities, isToday);

        // 좌석 응답 생성
        List<GroupedSeatResponse> groupedSeatResponses = seatAvailabilities.stream()
                .map(GroupedSeatResponse::of)
                .toList();

        return GroupedTimeSlotResponse.fromGroupedSeats(DateTimeUtil.formatToHHmm(timeslot), isAvailable, groupedSeatResponses);
    }
}