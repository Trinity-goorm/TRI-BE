package com.trinity.ctc.domain.notification.repository;

import com.trinity.ctc.domain.notification.entity.ReservationNotification;
import com.trinity.ctc.domain.notification.entity.type.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


public interface ReservationNotificationRepository extends JpaRepository<ReservationNotification, Long> {

    @Modifying
    @Transactional
    @Query("DELETE FROM ReservationNotification r WHERE r.reservation.id = :reservationId")
    void deleteAllByReservation(@Param("reservationId") long reservationId);

    @Query("Select r FROM ReservationNotification r WHERE r.type = :type AND DATE(r.scheduledTime) = :today")
    List<ReservationNotification> findAllByTypeAndDate(@Param("notificationType") NotificationType type,
                                                                @Param("scheduledTime") LocalDate today);

    @Query("Select r FROM ReservationNotification r WHERE r.type = :type AND DATE(r.scheduledTime) = :now")
    List<ReservationNotification> findAllByTypeAndDateTime(@Param("notificationType") NotificationType type,
                                                           @Param("scheduledTime") LocalDateTime now);
}
