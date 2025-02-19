package com.trinity.ctc.domain.notification.repository;

import com.trinity.ctc.domain.notification.entity.SeatNotificationMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SeatNotificationMessageRepository extends JpaRepository<SeatNotificationMessage, Long> {

    @Query("Select s FROM SeatNotificationMessage s WHERE s.seatAvailability.id = :seatAvailabilityId")
    Optional<SeatNotificationMessage> findBySeatId(@Param("seatAvailabilityId") long seatAvailabilityId);
}
