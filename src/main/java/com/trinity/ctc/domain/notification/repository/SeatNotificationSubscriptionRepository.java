package com.trinity.ctc.domain.notification.repository;

import com.trinity.ctc.domain.notification.entity.SeatNotificationSubscription;
import com.trinity.ctc.domain.notification.entity.SeatNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SeatNotificationSubscriptionRepository extends JpaRepository<SeatNotificationSubscription, Long> {

    @Query("Select s FROM SeatNotificationSubscription s WHERE s.user.id = :userId AND s.seatNotification = :seatNotification")
    Optional<SeatNotificationSubscription> findByUserId(@Param("userId") long userId, @Param("seatNotification") SeatNotification seatNotification);

    @Query("Select s FROM SeatNotificationSubscription s WHERE s.user.id = :userId")
    List<SeatNotificationSubscription> findAllByUserId(@Param("userId") long userId);

    @Query("SELECT COUNT(s) FROM SeatNotificationSubscription s WHERE s.seatNotification = :seatNotification")
    int countBySeatNotificationMessage(@Param("seatNotification") SeatNotification seatNotification);

    @Query("Select s FROM SeatNotificationSubscription s WHERE s.seatNotification.seat.id = :seatId")
    List<SeatNotificationSubscription> findAllBySeatId(@Param("seatId") long seatId);
}
