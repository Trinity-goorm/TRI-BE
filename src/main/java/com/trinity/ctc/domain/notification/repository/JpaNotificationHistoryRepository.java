package com.trinity.ctc.domain.notification.repository;

import com.trinity.ctc.domain.notification.entity.NotificationHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaNotificationHistoryRepository extends JpaRepository<NotificationHistory, Long> {
}
