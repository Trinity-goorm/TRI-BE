package com.trinity.ctc.domain.reservation.repository;

import com.trinity.ctc.domain.reservation.entity.Reservation;
import com.trinity.ctc.domain.reservation.status.ReservationStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
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
    boolean existsByReservationDataV1(@Param("userId") Long userId,
                                    @Param("restaurantId") Long restaurantId,
                                    @Param("selectedDate") LocalDate selectedDate,
                                    @Param("reservationTime") LocalTime reservationTime,
                                    @Param("seatTypeId") Long seatTypeId,
                                    @Param("statuses") List<ReservationStatus> statuses);

    @Query(value = """
        SELECT EXISTS (
            SELECT 1 FROM reservation r
            WHERE r.user_id = :userId
              AND r.restaurant_id = :restaurantId
              AND r.reservation_date = :selectedDate
              AND r.reservation_time_id = (SELECT rt.id FROM reservation_time rt WHERE rt.time_slot = :reservationTime)
              AND r.seat_type_id = :seatTypeId
              AND r.status IN (:statuses)
        )
    """, nativeQuery = true)
    boolean existsReservationNative(
            @Param("userId") Long userId,
            @Param("restaurantId") Long restaurantId,
            @Param("selectedDate") LocalDate selectedDate,
            @Param("reservationTime") LocalTime reservationTime,
            @Param("seatTypeId") Long seatTypeId,
            @Param("statuses") List<String> statuses
    );

    @Query("SELECT r FROM Reservation r " +
            "WHERE r.user.id = :userId " +
            "AND r.status IN (:statuses)")
    List<Reservation> findByUserIdAndStatusIn(@Param("userId") Long userId,
                                              @Param("statuses") List<ReservationStatus> statuses);


    @Query("SELECT r FROM Reservation r " +
            "JOIN FETCH r.restaurant rest " +
            "JOIN FETCH r.user u " +
            "JOIN FETCH r.reservationTime rt " +
            "JOIN FETCH r.seatType st " +
            "WHERE r.user.id = :userId")
    Slice<Reservation> findAllByUserId(@Param("userId") Long userId, Pageable pageable);

}
