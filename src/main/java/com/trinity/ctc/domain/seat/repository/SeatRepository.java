package com.trinity.ctc.domain.seat.repository;

import com.trinity.ctc.domain.restaurant.entity.Restaurant;
import com.trinity.ctc.domain.seat.dto.AvailableSeatPerDay;
import com.trinity.ctc.domain.seat.entity.Seat;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {

    @Query("SELECT sa FROM Seat sa " +
            "WHERE sa.restaurant.id = :restaurantId " +
            "AND sa.reservationTime.timeSlot > :targetTime " +
            "AND sa.reservationDate = CURRENT DATE")
    List<Seat> findAvailableSeatsToday(@Param("restaurantId") Long restaurantId,
                                       @Param("targetTime") LocalTime targetTime);

    @Query("SELECT new com.trinity.ctc.domain.seat.dto.AvailableSeatPerDay( " +
        "sa.restaurant.id, sa.reservationDate, sa.availableSeats, sa.reservationTime.timeSlot) " +
        "FROM Seat sa " +
        "WHERE sa.restaurant.id = :restaurantId " +
        "AND sa.reservationDate = :selectedDate")
    List<AvailableSeatPerDay> findAvailableSeatsForDate(@Param("restaurantId") Long restaurantId,
                                                        @Param("selectedDate") LocalDate selectedDate);

    @Query("SELECT sa FROM Seat sa " +
            "WHERE sa.restaurant.id = :restaurantId " +
            "AND sa.reservationDate = :selectedDate")
    List<Seat> findAvailableSeatsForDateEntity(@Param("restaurantId") Long restaurantId,
                                               @Param("selectedDate") LocalDate selectedDate);

    /**
     * seat 데이터 호출
     * @param restaurantId
     * @param selectedDate
     * @param reservationTime
     * @param seatTypeId
     * @return 예약데이터
     */
//    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT sa FROM Seat sa " +
            "WHERE sa.restaurant.id = :restaurantId " +
            "AND sa.reservationDate = :selectedDate " +
            "AND sa.reservationTime.timeSlot = :reservationTime " +
            "AND sa.seatType.id = :seatTypeId")
    Seat findByReservationData(@Param("restaurantId") Long restaurantId,
                               @Param("selectedDate") LocalDate selectedDate,
                               @Param("reservationTime") LocalTime reservationTime,
                               @Param("seatTypeId") Long seatTypeId);

    /**
     * 원자적으로 예약선점 수행
     * @param restaurantId
     * @param reservationDate
     * @param reservationTime
     * @param seatTypeId
     * @return 성공여부
     */
    @Modifying
    @Query("UPDATE Seat s SET s.availableSeats = s.availableSeats - 1 " +
            "WHERE s.restaurant.id = :restaurantId " +
            "AND s.reservationDate = :reservationDate " +
            "AND s.reservationTime.timeSlot = :reservationTime " +
            "AND s.seatType.id = :seatTypeId " +
            "AND s.availableSeats > 0")
    int preoccupySeat(@Param("restaurantId") Long restaurantId,
                      @Param("reservationDate") LocalDate reservationDate,
                      @Param("reservationTime") LocalTime reservationTime,
                      @Param("seatTypeId") Long seatTypeId);

    /**
     * 레디스 연산 결과 DB에 동기화
     * @param availableSeats
     * @param restaurantId
     * @param reservationDate
     * @param reservationTime
     * @param seatTypeId
     * @return
     */
    @Modifying
    @Query("UPDATE Seat s SET s.availableSeats = :availableSeats " +
            "WHERE s.restaurant.id = :restaurantId AND s.reservationDate = :reservationDate " +
            "AND s.reservationTime.timeSlot = :reservationTime AND s.seatType.id = :seatTypeId")
    int syncSeatWithRedis(@Param("availableSeats") Long availableSeats,
                           @Param("restaurantId") Long restaurantId,
                           @Param("reservationDate") LocalDate reservationDate,
                           @Param("reservationTime") LocalTime reservationTime,
                           @Param("seatTypeId") Long seatTypeId);


    @Query("SELECT COUNT(sa) > 0 FROM Seat sa WHERE sa.restaurant = :restaurant AND sa.reservationDate = :date")
    boolean existsByRestaurantAndReservationDate(Restaurant restaurant, LocalDate date);

    @Query("SELECT DISTINCT sa.restaurant.id FROM Seat sa WHERE sa.reservationDate = :date")
    Set<Long> findExistingRestaurantsWithAvailability(@Param("date") LocalDate date);
}
