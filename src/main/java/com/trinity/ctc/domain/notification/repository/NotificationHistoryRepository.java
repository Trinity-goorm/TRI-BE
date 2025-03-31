package com.trinity.ctc.domain.notification.repository;

import com.trinity.ctc.domain.notification.entity.NotificationHistory;

import java.util.List;

public interface NotificationHistoryRepository {
    void batchInsertNotificationHistories(List<NotificationHistory> notificationHistories);
}
