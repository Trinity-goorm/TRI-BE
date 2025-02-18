package com.trinity.ctc.domain.notification.repository;

import com.trinity.ctc.domain.notification.entity.NotificationHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationHistoryRepository extends JpaRepository<NotificationHistory, Long> {
}
