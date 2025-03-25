package com.trinity.ctc.domain.notification.service;

import com.google.common.collect.Lists;
import com.trinity.ctc.domain.fcm.entity.Fcm;
import com.trinity.ctc.domain.notification.entity.NotificationHistory;
import com.trinity.ctc.domain.notification.entity.ReservationNotification;
import com.trinity.ctc.domain.notification.fomatter.NotificationContentUtil;
import com.trinity.ctc.domain.notification.message.FcmMessage;
import com.trinity.ctc.domain.notification.repository.ReservationNotificationRepository;
import com.trinity.ctc.domain.notification.sender.NotificationSender;
import com.trinity.ctc.domain.reservation.entity.Reservation;
import com.trinity.ctc.domain.reservation.repository.ReservationRepository;
import com.trinity.ctc.domain.user.entity.User;
import com.trinity.ctc.domain.user.repository.UserRepository;
import com.trinity.ctc.global.exception.CustomException;
import com.trinity.ctc.global.exception.error_code.ReservationErrorCode;
import com.trinity.ctc.global.exception.error_code.UserErrorCode;
import com.trinity.ctc.global.util.formatter.DateTimeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.trinity.ctc.domain.notification.entity.ReservationNotification.createReservationNotification;
import static com.trinity.ctc.domain.notification.fomatter.NotificationMessageUtil.createMessageWithUrl;
import static com.trinity.ctc.domain.notification.type.NotificationType.*;
import static com.trinity.ctc.global.util.formatter.DateTimeUtil.combineWithDate;
import static com.trinity.ctc.global.util.formatter.DateTimeUtil.combineWithToday;

@Slf4j
@EnableAsync
@Service
@RequiredArgsConstructor
public class ReservationNotificationService {
    private final UserRepository userRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationNotificationRepository reservationNotificationRepository;
    private final NotificationSender notificationSender;
    private final NotificationHistoryService notificationHistoryService;

    /**
     * 예약 이벤트를 통해 예약 알림에 필요한 entity(user, reservation)를 받아오고, 예약 알림 entity을 DB에 저장하는 메서드
     *
     * @param userId        사용자
     * @param reservationId 예약 정보
     */
    @Transactional
    public void registerReservationNotification(long userId, long reservationId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new CustomException(UserErrorCode.NOT_FOUND));
        Reservation reservation = reservationRepository.findById(reservationId).orElseThrow(() -> new CustomException(ReservationErrorCode.NOT_FOUND));

        // 당일 예약 알림 메세지 포멧팅
        ReservationNotification dailyNotification = formattingDailyNotification(user, reservation);

        // 예약 1시간 전 알림 메세지 포멧팅
        ReservationNotification hourBeforeNotification = formattingHourBeforeNotification(user, reservation);

        List<ReservationNotification> notificationList = new ArrayList<>(Arrays.asList(dailyNotification, hourBeforeNotification));

        // 알림 2가지 예약 알림 table에 저장
        reservationNotificationRepository.saveAll(notificationList);
    }

    /**
     * registerReservationNotification()의 내부 메서드
     * 당일 예약 알림에 필요한 메세지를 포멧팅하고 예약 알림 entity build
     *
     * @param user        유저 Entity
     * @param reservation 예약 Entity
     * @return
     */
    private ReservationNotification formattingDailyNotification(User user, Reservation reservation) {
        // 당일 알림 메세지에 필요한 정보 변수 선언
        String userName = user.getNickname();
        String restaurantName = reservation.getRestaurant().getName();
        LocalDate reservedDate = reservation.getReservationDate();
        LocalTime reservedTime = reservation.getReservationTime().getTimeSlot();
        LocalDateTime scheduledTime = combineWithDate(reservedDate, LocalTime.of(8, 0));

        // 알림 메세지 data 별 포멧팅
        String title = NotificationContentUtil.formatDailyNotificationTitle(userName, restaurantName);
        String body = NotificationContentUtil.formatDailyNotificationBody(restaurantName, reservedDate, reservedTime);
        String url = NotificationContentUtil.formatReservationNotificationUrl();

        // 알림 메세지 빌드
        return createReservationNotification(DAILY_NOTIFICATION, user, reservation, title, body, url, scheduledTime);
    }

    /**
     * registerReservationNotification()의 내부 메서드
     * 1시간 전 예약 알림에 필요한 메세지를 포멧팅하고 예약 알림 entity build
     *
     * @param user        유저 Entity
     * @param reservation 예약 Entity
     * @return
     */
    private ReservationNotification formattingHourBeforeNotification(User user, Reservation reservation) {
        // 당일 알림 메세지에 필요한 정보 변수 선언
        String userName = user.getNickname();
        String restaurantName = reservation.getRestaurant().getName();
        LocalDate reservedDate = reservation.getReservationDate();
        LocalTime reservedTime = reservation.getReservationTime().getTimeSlot();
        LocalDateTime scheduledTime = DateTimeUtil.combineWithDate(reservedDate, reservedTime).minusHours(1);

        // 알림 메세지 data 별 포멧팅
        String title = NotificationContentUtil.formatHourBeforeNotificationTitle(userName, restaurantName);
        String body = NotificationContentUtil.formatHourBeforeNotificationBody(restaurantName, reservedDate, reservedTime);
        String url = NotificationContentUtil.formatReservationNotificationUrl();

        // 알림 메세지 빌드

        return createReservationNotification(BEFORE_ONE_HOUR_NOTIFICATION, user, reservation, title, body, url, scheduledTime);
    }

    /**
     * 예약 취소 시, 해당 예약에 대한 알림을 취소하는 메서드
     *
     * @param reservationId 예약 ID
     */
    @Transactional
    public void deleteReservationNotification(Long reservationId) {
        reservationNotificationRepository.deleteAllByReservationId(reservationId);
    }

    /**
     * 매일 8시에 당일 예약 알림을 보내는 메서드
     */
    @Async
    public void sendDailyNotification(LocalDate scheduledDate) {
        log.info("✅ 당일 예약 알림 발송 시작!");
        long startTime = System.nanoTime(); // 시작 시간 측정

        int batchSize = 500;
        int batchCount = 0;

        List<FcmMessage> messageList = new ArrayList<>();

        List<CompletableFuture<List<NotificationHistory>>> resultList = new ArrayList<>();

        // 알림 타입과 오늘 날짜로 당일 예약 알림 정보 가져오기
        List<ReservationNotification> reservationNotificationList = reservationNotificationRepository
                .findAllByTypeAndScheduledDate(DAILY_NOTIFICATION, scheduledDate);
        log.info("가져온 목록: " + reservationNotificationList.size());
        if (reservationNotificationList.isEmpty()) return;



        // 전송할 알림 리스트를 전부 도는 알림 발송 로직
        for (ReservationNotification notification : reservationNotificationList) {
            messageList.addAll(buildReservationNotification(notification));
        }

        List<List<FcmMessage>> batches = Lists.partition(messageList, batchSize);
        int clearCount = batches.size();

        for (List<FcmMessage> batch : batches) {
            batchCount++;
            CompletableFuture<List<NotificationHistory>> sendingResult = sendReservationNotification(batch, batchCount, clearCount);
            resultList.add(sendingResult);
        }

        long endTime = System.nanoTime();  // 종료 시간 측정
        long elapsedTime = endTime - startTime;  // 경과 시간 (나노초 단위)

        log.info("당일 예약 알림 발송 실행 시간: {} ms", elapsedTime / 1_000_000);

        List<NotificationHistory> notificationHistoryList = resultList.stream()
                .map(CompletableFuture::join) // 각 future의 실행이 끝날 때까지 기다림
                .flatMap(List::stream) // 여러 리스트를 하나로 합침
                .toList();

        // 전송된 알림 히스토리를 전부 history 테이블에 저장하는 메서드
        notificationHistoryService.saveNotificationHistory(notificationHistoryList);
        // 해당 날짜에 스케줄된 당일 예약 알림을 table에서 삭제하는 메서드
//        deleteDailyNReservationNotification();
    }

    /**
     * 예약 1시간 전 알림을 보내는 메서드
     */
    @Async
    public void sendHourBeforeNotification(LocalDateTime scheduledTime) {
        log.info("✅ 한시간 전 예약 알림 발송 시작!");
        long startTime = System.nanoTime(); // 시작 시간 측정

        int batchSize = 500;
        int batchCount = 0;

        List<CompletableFuture<List<NotificationHistory>>> resultList = new ArrayList<>();

        List<FcmMessage> messageList = new ArrayList<>();

        // 알림 타입과 현재 시간으로 보낼 예약 1시간 전 알림 정보 가져오기
        List<ReservationNotification> reservationNotificationList = reservationNotificationRepository
                .findAllByTypeAndScheduledTime(BEFORE_ONE_HOUR_NOTIFICATION, scheduledTime);

        // 전송할 알림 리스트를 전부 도는 알림 발송 로직
        for (ReservationNotification notification : reservationNotificationList) {
            messageList.addAll(buildReservationNotification(notification));
        }

        List<List<FcmMessage>> batches = Lists.partition(messageList, batchSize);
        int clearCount = batches.size();

        for (List<FcmMessage> batch : batches) {
            batchCount++;
            CompletableFuture<List<NotificationHistory>> sendingResult = sendReservationNotification(batch, batchCount, clearCount);
            resultList.add(sendingResult);
        }

        long endTime = System.nanoTime();  // 종료 시간 측정
        long elapsedTime = endTime - startTime;  // 경과 시간 (나노초 단위)

        log.info("한시간 전 예약 알림 발송 실행 시간: {} ms", elapsedTime / 1_000_000);

        List<NotificationHistory> notificationHistoryList = resultList.stream()
                .map(CompletableFuture::join) // 각 future의 실행이 끝날 때까지 기다림
                .flatMap(List::stream) // 여러 리스트를 하나로 합침
                .toList();
        // 전송된 알림 히스토리를 전부 history 테이블에 저장하는 메서드
        notificationHistoryService.saveNotificationHistory(notificationHistoryList);
        // 해당 시간에 스케줄된 한시간 전 예약 알림을 table에서 삭제하는 메서드
//        deleteHourlyNReservationNotification(scheduledTime);
    }

    public CompletableFuture<List<NotificationHistory>> sendReservationNotification(List<FcmMessage> fcmMessageList, int batchCount, int clearCount) {
        return notificationSender.sendEachNotification(fcmMessageList)
                .thenApplyAsync(resultList -> {
                    log.info("✅ 빈자리 알림 발송 완료 Batch {}", batchCount);
                    if (batchCount == clearCount) log.info("전송완료!!!!!!!!!!!!!");

                    return notificationHistoryService.buildNotificationHistory(fcmMessageList, resultList, SEAT_NOTIFICATION);
                });
    }

    /**
     * 알림 전송 로직 중 FCM 메세지를 빌드하는 내부 메서드
     *
     * @param notification 예약 알림 Entity
     * @return
     */
    private List<FcmMessage> buildReservationNotification(ReservationNotification notification) {
        List<FcmMessage> messages = new ArrayList<>();

        for (Fcm fcm : notification.getUser().getFcmList()) {
            FcmMessage message = createMessageWithUrl(notification.getTitle(), notification.getBody(), notification.getUrl(), fcm);
            messages.add(message);
        }
        return messages;
    }

    private void deleteDailyNReservationNotification() {
        LocalDateTime scheduledTime = combineWithToday(LocalTime.of(8, 0));
        reservationNotificationRepository.deleteAllByScheduledTimeAndType(scheduledTime, DAILY_NOTIFICATION);
    }

    private void deleteHourlyNReservationNotification(LocalDateTime scheduledTime) {
        reservationNotificationRepository.deleteAllByScheduledTimeAndType(scheduledTime, BEFORE_ONE_HOUR_NOTIFICATION);
    }
}
