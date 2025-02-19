package com.trinity.ctc.domain.reservation.repository;

import com.trinity.ctc.domain.reservation.entity.Reservation;
import com.trinity.ctc.domain.reservation.status.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM Reservation r " +
            "WHERE r.user.id = :userId " +
            "AND r.restaurant.id = :restaurantId " +
            "AND r.reservationDate = :selectedDate " +
            "AND r.reservationTime.timeSlot = :reservationTime " +
            "AND r.seatType.id = :seatTypeId " +
            "AND r.status IN (:statuses)")
    boolean existsByReservationData(@Param("userId") Long userId,
                                    @Param("restaurantId") Long restaurantId,
                                    @Param("selectedDate") LocalDate selectedDate,
                                    @Param("reservationTime") LocalTime reservationTime,
                                    @Param("seatTypeId") Long seatTypeId,
                                    @Param("statuses") List<ReservationStatus> statuses);
}
