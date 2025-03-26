package com.trinity.ctc.domain.notification.Factory;

import com.trinity.ctc.domain.notification.entity.ReservationNotification;
import com.trinity.ctc.domain.notification.entity.SeatNotification;
import com.trinity.ctc.domain.notification.entity.SeatNotificationSubscription;
import com.trinity.ctc.domain.notification.repository.SeatNotificationRepository;
import com.trinity.ctc.domain.notification.type.NotificationType;
import com.trinity.ctc.domain.reservation.repository.ReservationRepository;
import com.trinity.ctc.domain.seat.repository.SeatRepository;
import com.trinity.ctc.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.trinity.ctc.global.util.formatter.DateTimeUtil.convertToLocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationFactory {

    private final UserRepository userRepository;
    private final ReservationRepository reservationRepository;
    private final SeatRepository seatRepository;
    private final SeatNotificationRepository seatNotificationRepository;

    public List<ReservationNotification> createReservationNotificationsByCsv(List<Map<String, String>> reservationNotificationCsv) {
        List<ReservationNotification> reservationNotifications = new ArrayList<>();
        for (Map<String, String> row : reservationNotificationCsv) {

            ReservationNotification reservationNotification = ReservationNotification.builder()
                    .type(NotificationType.valueOf(row.get("type")))
                    .title(row.get("title"))
                    .body(row.get("body"))
                    .url(row.get("url"))
                    .scheduledTime(convertToLocalDateTime(row.get("scheduled_time")))
                    .user(userRepository.findById(Long.parseLong(row.get("user_id"))).orElse(null))
                    .reservation(reservationRepository.findById(Long.parseLong(row.get("reservation_id"))).orElse(null))
                    .build();

            reservationNotifications.add(reservationNotification);
        }

        log.info("저장 완료");
        return reservationNotifications;
    }

    public List<SeatNotification> createSeatNotificationsByCsv(List<Map<String, String>> seatNotificationCsv) {
        List<SeatNotification> seatNotifications = new ArrayList<>();
        for (Map<String, String> row : seatNotificationCsv) {
            SeatNotification seatNotification = SeatNotification.builder()
                    .title(row.get("title"))
                    .body(row.get("body"))
                    .url(row.get("url"))
                    .seat(seatRepository.findById(Long.parseLong(row.get("seat_id"))).orElse(null))
                    .build();

            seatNotifications.add(seatNotification);
        }
        return seatNotifications;
    }

    public List<SeatNotificationSubscription> createSeatNotificationSubscriptionsByCsv(List<Map<String, String>> seatNotificationSubscriptionCsv) {
        List<SeatNotificationSubscription> seatNotificationSubscriptions = new ArrayList<>();
        for (Map<String, String> row : seatNotificationSubscriptionCsv) {
            SeatNotificationSubscription seatNotificationSubscription = SeatNotificationSubscription.builder()
                    .user(userRepository.findById(Long.parseLong(row.get("user_id"))).orElse(null))
                    .seatNotification(seatNotificationRepository.findById(Long.parseLong(row.get("seat_notification_id"))).orElse(null))
                    .build();

            seatNotificationSubscriptions.add(seatNotificationSubscription);
        }
        return seatNotificationSubscriptions;
    }
}