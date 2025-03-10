package com.trinity.ctc.domain.notification.repository;

import com.trinity.ctc.domain.notification.entity.SeatNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SeatNotificationRepository extends JpaRepository<SeatNotification, Long> {

    @Query("SELECT s FROM SeatNotification s WHERE s.seat.id = :seatId")
    Optional<SeatNotification> findBySeatId(@Param("seatId") long seatId);


    @Query("SELECT s FROM SeatNotification s WHERE s.seat IN (" +
            "    SELECT a FROM Seat a WHERE a.reservationDate < :currentDate OR " +
            "         (a.reservationDate = :currentDate AND a.reservationTime.timeSlot < :currentTime))")
    List<SeatNotification> findAllByCurrentDateTime(@Param("currentDate") LocalDate currentDate,
                                                    @Param("currentTime") LocalTime currentTime);
}
