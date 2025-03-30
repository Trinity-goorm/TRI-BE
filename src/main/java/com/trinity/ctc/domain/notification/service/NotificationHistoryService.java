package com.trinity.ctc.domain.notification.service;

import com.trinity.ctc.domain.notification.entity.NotificationHistory;
import com.trinity.ctc.domain.notification.repository.JpaNotificationHistoryRepository;
import com.trinity.ctc.domain.notification.repository.NotificationHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;

@Slf4j
@Service
@EnableAsync
@RequiredArgsConstructor
public class NotificationHistoryService {
    private final JpaNotificationHistoryRepository jpaNotificationHistoryRepository;
    private final NotificationHistoryRepository notificationHistoryRepository;

    @Transactional(propagation = REQUIRES_NEW)
    public void saveNotificationHistory(List<NotificationHistory> list) {
        if (list.size() <= 50) {
            saveFewNotificationHistory(list);
        } else {
            saveBatchNotificationHistory(list);
        }
    }

    @Transactional
    public void saveSingleNotificationHistory(NotificationHistory history) {
        jpaNotificationHistoryRepository.save(history);
    }

    /**
     * 전송된 알림 히스토리를 전부 history 테이블에 저장하는 메서드
     * 소량의 발송 건에 대해 jpa saveAll로 저장
     * @param notificationHistoryList 알림 history 리스트
     */
    @Async("save-notification-history")
    public void saveFewNotificationHistory(List<NotificationHistory> notificationHistoryList) {
        jpaNotificationHistoryRepository.saveAll(notificationHistoryList);
    }

    /**
     * 전송된 알림 히스토리를 전부 history 테이블에 저장하는 메서드
     * 대량의 발송 건에 대해 jdbc batch insert 로 저장
     * @param notificationHistoryList 알림 history 리스트
     */
    @Async("save-notification-history")
    public void saveBatchNotificationHistory(List<NotificationHistory> notificationHistoryList) {
        notificationHistoryRepository.batchInsertNotificationHistories(notificationHistoryList);
    }
}
