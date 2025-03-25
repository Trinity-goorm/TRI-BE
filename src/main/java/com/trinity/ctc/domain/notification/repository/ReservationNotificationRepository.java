package com.trinity.ctc.domain.notification.repository;

import com.trinity.ctc.domain.notification.entity.ReservationNotification;
import com.trinity.ctc.domain.notification.type.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReservationNotificationRepository extends JpaRepository<ReservationNotification, Long> {

    @Modifying
    @Transactional
    @Query("DELETE FROM ReservationNotification r WHERE r.reservation.id = :reservationId")
    void deleteAllByReservationId(@Param("reservationId") long reservationId);

    @Query("Select r FROM ReservationNotification r join fetch r.user u join fetch r.user.fcmList f left join fetch r.user.userPreference uf WHERE r.type = :notificationType AND DATE(r.scheduledTime) = :scheduledDate")
    List<ReservationNotification> findAllByTypeAndScheduledDate(@Param("notificationType") NotificationType notificationtype,
                                                                @Param("scheduledDate") LocalDate scheduledDate);

    @Query("SELECT r FROM ReservationNotification r join fetch r.user u join fetch r.user.fcmList f left join fetch r.user.userPreference uf WHERE r.type = :notificationType AND r.scheduledTime = :scheduledTime")
    List<ReservationNotification> findAllByTypeAndScheduledTime(@Param("notificationType") NotificationType notificationType,
                                                                @Param("scheduledTime") LocalDateTime scheduledTime);

    void deleteAllByScheduledTimeAndType(LocalDateTime scheduledTime, NotificationType type);
}
