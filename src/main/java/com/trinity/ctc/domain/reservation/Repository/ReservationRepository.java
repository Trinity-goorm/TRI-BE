package com.trinity.ctc.domain.reservation.Repository;

import com.trinity.ctc.domain.reservation.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
}
