package com.trinity.ctc.domain.notification.repository;

import com.trinity.ctc.domain.notification.entity.SeatNotificationMessage;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SeatNotificationMessageRepository extends JpaRepository<SeatNotificationMessage, Long> {

    @Query("SELECT s FROM SeatNotificationMessage s WHERE s.seat.id = :seatId")
    Optional<SeatNotificationMessage> findBySeatId(@Param("seatId") long seatId);


    @Query("SELECT s FROM SeatNotificationMessage s WHERE s.seat IN (" +
            "    SELECT a FROM Seat a WHERE a.reservationDate < :currentDate OR " +
            "         (a.reservationDate = :currentDate AND a.reservationTime.timeSlot < :currentTime))")
    List<SeatNotificationMessage> findAllByCurrentDateTime(@Param("currentDate") LocalDate currentDate,
                                                                  @Param("currentTime") LocalTime currentTime);
}
