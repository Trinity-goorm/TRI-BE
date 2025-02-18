package com.trinity.ctc.domain.notification.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.trinity.ctc.domain.fcm.repository.FcmRepository;
import com.trinity.ctc.domain.notification.dto.FcmSendingResultDto;
import com.trinity.ctc.domain.notification.entity.NotificationHistory;
import com.trinity.ctc.domain.notification.entity.ReservationNotification;
import com.trinity.ctc.domain.notification.entity.result.SentResult;
import com.trinity.ctc.domain.notification.entity.type.NotificationType;
import com.trinity.ctc.domain.notification.repository.NotificationHistoryRepository;
import com.trinity.ctc.domain.notification.repository.ReservationNotificationRepository;
import com.trinity.ctc.domain.notification.util.NotificationMessageUtil;
import com.trinity.ctc.domain.reservation.entity.Reservation;
import com.trinity.ctc.domain.reservation.repository.ReservationRepository;
import com.trinity.ctc.domain.restaurant.repository.RestaurantRepository;
import com.trinity.ctc.domain.user.entity.User;
import com.trinity.ctc.event.ReservationCanceledEvent;
import com.trinity.ctc.event.ReservationSuccessEvent;
import com.trinity.ctc.kakao.repository.UserRepository;
import com.trinity.ctc.util.exception.CustomException;
import com.trinity.ctc.util.exception.error_code.ReservationErrorCode;
import com.trinity.ctc.util.exception.error_code.UserErrorCode;
import com.trinity.ctc.util.formatter.DateTimeUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import static com.trinity.ctc.util.formatter.DateTimeUtil.combineWithDate;

@EnableAsync
@Service
@RequiredArgsConstructor
public class NotificationService {
    private final UserRepository userRepository;
    private final FcmRepository fcmRepository;
    private final ReservationRepository reservationRepository;
    private final NotificationHistoryRepository notificationHistoryRepository;
    private final ReservationNotificationRepository reservationNotificationRepository;

    /**
     * 예약 이벤트를 통해 예약 알림에 필요한 entity(user, reservation)를 받아오고, 예약 알림 entity을 DB에 저장하는 메서드
     * @param reservationEvent
     */
    public void registerReservationNotification(ReservationSuccessEvent reservationEvent) {
        User user = userRepository.findById(reservationEvent.getUserId()).orElseThrow(()
                -> new CustomException(UserErrorCode.NOT_FOUND));

        Reservation reservation = reservationRepository.findById(reservationEvent.getReservationId()).orElseThrow(()
                -> new CustomException(ReservationErrorCode.NOT_FOUND));

        ReservationNotification dailyNotification = formattingDailyNotification(user, reservation);
        ReservationNotification hourBeforeNotification = formattingHourBeforeNotification(user, reservation);

        List<ReservationNotification> notificationList = new ArrayList<>(Arrays.asList(dailyNotification, hourBeforeNotification));

        reservationNotificationRepository.saveAll(notificationList);
    }

    /**
     * registerReservationNotification()의 내부 메서드
     * 당일 예약 알림에 필요한 메세지를 포멧팅하고 예약 알림 entity build
     * @param user
     * @param reservation
     * @return
     */
    private ReservationNotification formattingDailyNotification(User user, Reservation reservation) {
        String userName = user.getNickname();
        String restaurantName = reservation.getRestaurant().getName();
        LocalDate reservedDate = reservation.getReservationDate();
        LocalTime reservedTime = reservation.getReservationTime().getTimeSlot();
        Long reservationId = reservation.getId();
        LocalDateTime scheduledTime = combineWithDate(reservedDate, LocalTime.of(8, 0));

        String title = NotificationMessageUtil.formatDailyNotificationTitle(userName, restaurantName);
        String body = NotificationMessageUtil.formatDailyNotificationBody(restaurantName, reservedDate, reservedTime);
        String url = NotificationMessageUtil.formatReservationNotificationUrl(reservationId);

        ReservationNotification reservationNotification = ReservationNotification.builder()
                .type(NotificationType.DAILY_NOTIFICATION)
                .title(title)
                .body(body)
                .url(url)
                .user(user)
                .scheduledTime(scheduledTime)
                .reservation(reservation)
                .build();

        return reservationNotification;
    }

    /**
     * registerReservationNotification()의 내부 메서드
     * 1시간 전 예약 알림에 필요한 메세지를 포멧팅하고 예약 알림 entity build
     * @param user
     * @param reservation
     * @return
     */
    private ReservationNotification formattingHourBeforeNotification(User user, Reservation reservation) {
        String userName = user.getNickname();
        String restaurantName = reservation.getRestaurant().getName();
        LocalDate reservedDate = reservation.getReservationDate();
        LocalTime reservedTime = reservation.getReservationTime().getTimeSlot();
        Long reservationId = reservation.getId();
        LocalDateTime scheduledTime = DateTimeUtil.combineWithDate(reservedDate, reservedTime).minusHours(1);


        String title = NotificationMessageUtil.formatHourBeforeNotificationTitle(userName, restaurantName);
        String body = NotificationMessageUtil.formatHourBeforeNotificationBody(restaurantName, reservedDate, reservedTime);
        String url = NotificationMessageUtil.formatReservationNotificationUrl(reservationId);

        ReservationNotification reservationNotification = ReservationNotification.builder()
                .type(NotificationType.DAILY_NOTIFICATION)
                .title(title)
                .body(body)
                .url(url)
                .user(user)
                .scheduledTime(scheduledTime)
                .reservation(reservation)
                .build();

        return reservationNotification;
    }

    /**
     * 예약 취소 시, 해당 예약에 대한 알림을 취소하는 메서드
     * @param reservationEvent
     */
    public void deleteReservationNotification(ReservationCanceledEvent reservationEvent) {
        long reservationId = reservationEvent.getReservation().getId();
        reservationNotificationRepository.deleteAllByReservation(reservationId);
    }

    /**
     * 매일 8시에 당일 예약 알림을 보내는 메서드(매일 8시에 실행되도록 스케줄링)
     */
    @Scheduled(cron = "0 0 8 * * ?") // 매일 8시에 실행
    private void sendDailyReservationNotification() {
        LocalDate today = LocalDate.now();
        List<ReservationNotification> reservationNotificationList = reservationNotificationRepository
                .findAllByTypeAndDate(NotificationType.DAILY_NOTIFICATION, today);
        List<NotificationHistory> notificationHistoryList = new ArrayList<>();
        List<Long> reservationNotificationIdList = new ArrayList<>();

        NotificationType type = NotificationType.DAILY_NOTIFICATION;

        for(ReservationNotification notification: reservationNotificationList) {
            NotificationHistory notificationHistory = handleEachNotification(notification, type);
            notificationHistoryList.add(notificationHistory);
            reservationNotificationIdList.add(notification.getId());
        }
        processNotificationData(notificationHistoryList, reservationNotificationIdList);
    }


    /**
     * 예약 1시간 전 알림을 보내는 메서드(운영시간 내에서 1시간 단위로 실행되도록 스케줄링)
     */
    @Scheduled(cron = "0 0 11-23/1 * *  ?") // 11 ~ 23시 동안 1시간 단위로 실행
    private void sendHourBeforeReservationNotification() {
        LocalDateTime now = DateTimeUtil.truncateToMinute(LocalDateTime.now());

        List<ReservationNotification> reservationNotificationList = reservationNotificationRepository
                .findAllByTypeAndDateTime(NotificationType.BEFORE_ONE_HOUR_NOTIFICATION, now);
        List<NotificationHistory> notificationHistoryList = new ArrayList<>();
        List<Long> reservationNotificationIdList = new ArrayList<>();

        NotificationType type = NotificationType.BEFORE_ONE_HOUR_NOTIFICATION;

        for(ReservationNotification notification: reservationNotificationList) {
            NotificationHistory notificationHistory = handleEachNotification(notification, type);
            notificationHistoryList.add(notificationHistory);
            reservationNotificationIdList.add(notification.getId());
        }
        processNotificationData(notificationHistoryList, reservationNotificationIdList);
    }

    /**
     * 단 건의 알림 전송 로직에 대해 처리하는 메서드
     * @param notification
     * @param type
     * @return
     */
    private NotificationHistory handleEachNotification(ReservationNotification notification, NotificationType type) {
        Message message = buildReservationNotification(notification);
        FcmSendingResultDto result = sendNotification(message);
        return buildNotificationHistory(notification, result, type);
    }

    /**
     * 알림 전송 로직 중 FCM 메세지를 빌드하는 내부 메서드
     * @param notification
     * @return
     */
    private Message buildReservationNotification(ReservationNotification notification) {
        String token = fcmRepository.findByUser(notification.getUser().getId());

        Message message = Message.builder()
                .putData("title", notification.getTitle())
                .putData("body", notification.getBody())
                .putData("url", notification.getUrl())
                .setToken(token)
                .build();

        return message;
    }

    /**
     * 알림 전송 로직 중 FCM 메세지를 발송하는 내부 메서드
     * @param message
     * @return
     */
    private FcmSendingResultDto sendNotification(Message message) {
        FcmSendingResultDto result;

        try {
            FirebaseMessaging.getInstance().send(message);
            result = new FcmSendingResultDto(LocalDateTime.now(), SentResult.SUCCESS);
        } catch (FirebaseMessagingException e) {
            result = new FcmSendingResultDto(LocalDateTime.now(), SentResult.FAILED, e.getMessagingErrorCode());
        }
        return result;
    }

    private void sendMulticastNotification() {

    }

    /**
     * 알림 전송 로직 중 전송된 알림에 대한 히스토리를 빌드하는 내부 메서드
     * @param notification
     * @param result
     * @param type
     * @return
     */
    private NotificationHistory buildNotificationHistory(ReservationNotification notification,
                                                         FcmSendingResultDto result, NotificationType type) {
        Map<String, String> messageHistory = new HashMap<>();
        messageHistory.put("title", notification.getTitle());
        messageHistory.put("body", notification.getBody());
        messageHistory.put("url", notification.getUrl());
        
        NotificationHistory notificationHistory = NotificationHistory.builder()
                .type(type)
                .message(messageHistory)
                .sentAt(result.getSentAt())
                .sentResult(result.getSentResult())
                .errorCode(result.getErrorCode())
                .user(notification.getUser())
                .build();
        return notificationHistory;
    }

    /**
     * 전송된 알림 히스토리를 전부 history 테이블에 저장하고  메서드
     * @param notificationHistoryList
     */
    private void processNotificationData (List<NotificationHistory> notificationHistoryList,
                                              List<Long> reservationNotificationIdList) {
        notificationHistoryRepository.saveAll(notificationHistoryList);
        reservationNotificationRepository.deleteAllById(reservationNotificationIdList);
    }
}
