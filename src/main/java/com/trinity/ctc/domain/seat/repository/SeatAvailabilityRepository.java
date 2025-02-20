package com.trinity.ctc.domain.seat.repository;

import com.trinity.ctc.domain.restaurant.entity.Restaurant;
import com.trinity.ctc.domain.seat.entity.SeatAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

@Repository
public interface SeatAvailabilityRepository extends JpaRepository<SeatAvailability, Long> {

    @Query("SELECT sa FROM SeatAvailability sa " +
            "WHERE sa.restaurant.id = :restaurantId " +
            "AND sa.reservationTime.timeSlot > :targetTime " +
            "AND sa.reservationDate = CURRENT DATE")
    List<SeatAvailability> findAvailableSeatsToday(@Param("restaurantId") Long restaurantId,
                                              @Param("targetTime") LocalTime targetTime);

    @Query("SELECT sa FROM SeatAvailability sa " +
            "WHERE sa.restaurant.id = :restaurantId " +
            "AND sa.reservationDate = :selectedDate")
    List<SeatAvailability> findAvailableSeatsForDate(@Param("restaurantId") Long restaurantId,
                                                     @Param("selectedDate") LocalDate selectedDate);

    @Query("SELECT sa FROM SeatAvailability sa " +
            "WHERE sa.restaurant.id = :restaurantId " +
            "AND sa.reservationDate = :selectedDate " +
            "AND sa.reservationTime.timeSlot = :reservationTime " +
            "AND sa.seatType.id = :seatTypeId" )
    SeatAvailability findByReservationData(@Param("restaurantId") Long restaurantId,
                                           @Param("selectedDate") LocalDate selectedDate,
                                           @Param("reservationTime") LocalTime reservationTime,
                                           @Param("seatTypeId") Long seatTypeId);

    @Query("SELECT COUNT(sa) > 0 FROM SeatAvailability sa WHERE sa.restaurant = :restaurant AND sa.reservationDate = :date")
    boolean existsByRestaurantAndReservationDate(Restaurant restaurant, LocalDate date);

    @Query("SELECT DISTINCT sa.restaurant.id FROM SeatAvailability sa WHERE sa.reservationDate = :date")
    Set<Long> findExistingRestaurantsWithAvailability(@Param("date") LocalDate date);
}
