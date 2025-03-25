package com.trinity.ctc.domain.notification.service;

import com.trinity.ctc.domain.notification.entity.NotificationHistory;
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
    private final NotificationHistoryRepository notificationHistoryRepository;

    /**
     * 전송된 알림 히스토리를 전부 history 테이블에 저장하는 메서드
     * @param notificationHistoryList 알림 history 리스트
     */
    @Transactional(propagation = REQUIRES_NEW)
    @Async("save-notification-history")
    public void saveNotificationHistory(List<NotificationHistory> notificationHistoryList) {
        notificationHistoryRepository.saveAll(notificationHistoryList);
    }
}
