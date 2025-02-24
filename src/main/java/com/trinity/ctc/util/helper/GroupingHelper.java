package com.trinity.ctc.util.helper;

import com.trinity.ctc.domain.seat.entity.Seat;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GroupingHelper {
    private GroupingHelper() {}

    public static Map<LocalTime, List<Seat>> groupByTimeSlot(List<Seat> seats) {
        return seats.stream()
                .collect(Collectors.groupingBy(sa -> sa.getReservationTime().getTimeSlot()));
    }
}
