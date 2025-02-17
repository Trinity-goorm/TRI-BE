package com.trinity.ctc.domain.notification.repository;

import com.trinity.ctc.domain.notification.entity.ReservationNotification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationNotificationRepository extends JpaRepository<ReservationNotification, Long> {
}
