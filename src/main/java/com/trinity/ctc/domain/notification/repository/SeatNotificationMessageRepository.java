package com.trinity.ctc.domain.notification.repository;

import com.trinity.ctc.domain.notification.entity.SeatNotificationMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeatNotificationMessageRepository extends JpaRepository<SeatNotificationMessage, Long> {
}
