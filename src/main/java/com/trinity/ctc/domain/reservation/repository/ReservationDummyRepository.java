package com.trinity.ctc.domain.reservation.repository;

import com.trinity.ctc.domain.reservation.entity.Reservation;

import java.util.List;

public interface ReservationDummyRepository {
    void batchInsertReservations(List<Reservation> reservation, int batchSize);
}
