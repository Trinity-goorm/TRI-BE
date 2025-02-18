package com.trinity.ctc.domain.notification.repository;

import com.trinity.ctc.domain.notification.entity.SeatNotification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeatNotificationRepository extends JpaRepository<SeatNotification, Long> {
}
