package com.trinity.ctc.domain.reservation.repository;

import com.trinity.ctc.domain.reservation.entity.ReservationTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.Optional;

@Repository
public interface ReservationTimeRepository extends JpaRepository<ReservationTime, Long> {

    Optional<ReservationTime> findByTimeSlot(LocalTime timeSlot);
}
