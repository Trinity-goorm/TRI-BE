package com.trinity.ctc.domain.seat.repository;

import com.trinity.ctc.domain.seat.entity.Seat;

import java.util.List;

public interface SeatBatchRepository {
    void batchInsertSeats(List<Seat> seats, int batchSize);
}
