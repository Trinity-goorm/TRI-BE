package com.trinity.ctc.domain.notification.service;

import com.trinity.ctc.domain.notification.Factory.NotificationFactory;
import com.trinity.ctc.domain.notification.entity.ReservationNotification;
import com.trinity.ctc.domain.notification.entity.SeatNotification;
import com.trinity.ctc.domain.notification.entity.SeatNotificationSubscription;
import com.trinity.ctc.domain.notification.repository.NotificationDummyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationDummyService {
    private final NotificationDummyRepository notificationDummyRepository;
    private final NotificationFactory notificationFactory;

    @Transactional
    public void generateDummyData(
            List<Map<String, String>> reservationNotificationCsv,
            List<Map<String, String>> seatNotificationCsv,
            List<Map<String, String>> seatNotificationSubscriptionCsv,
            int batchSize
    ) {
        log.info("✅ [NotificationDummyService] Notification CSV 데이터 파싱 및 생성 시작");
        List<ReservationNotification> reservationNotifications = notificationFactory.createReservationNotificationsByCsv(reservationNotificationCsv);
        List<SeatNotification> seatNotifications = notificationFactory.createSeatNotificationsByCsv(seatNotificationCsv);
        List<SeatNotificationSubscription> seatNotificationSubscriptions = notificationFactory.createSeatNotificationSubscriptionsByCsv(seatNotificationSubscriptionCsv);
        log.info("✅ [NotificationDummyService] 생성된 reservationNotification 개수: {}", reservationNotifications.size());
        log.info("✅ [NotificationDummyService] 생성된 seatNotification 개수: {}", seatNotifications.size());
        log.info("✅ [NotificationDummyService] 생성된 seatNotificationSubscription 개수: {}", seatNotificationSubscriptions.size());

        log.info("✅ [NotificationDummyService] Notification 배치 저장 시작");
        notificationDummyRepository.batchInsertReservationNotifications(reservationNotifications, batchSize);
        notificationDummyRepository.batchInsertSeatNotifications(seatNotifications, batchSize);
        notificationDummyRepository.batchInsertSeatNotificationSubscriptions(seatNotificationSubscriptions, batchSize);
        log.info("✅ [NotificationDummyService] Notification 배치 저장 완료");
    }
}
