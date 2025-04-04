package com.trinity.ctc.domain.notification.service;

import com.google.common.collect.Lists;
import com.trinity.ctc.domain.fcm.entity.Fcm;
import com.trinity.ctc.domain.notification.entity.NotificationHistory;
import com.trinity.ctc.domain.notification.entity.ReservationNotification;
import com.trinity.ctc.domain.notification.message.FcmMessage;
import com.trinity.ctc.domain.notification.repository.ReservationNotificationRepository;
import com.trinity.ctc.domain.notification.sender.NotificationSenderV1;
import com.trinity.ctc.domain.notification.type.NotificationType;
import com.trinity.ctc.domain.reservation.entity.Reservation;
import com.trinity.ctc.domain.reservation.repository.ReservationRepository;
import com.trinity.ctc.domain.user.entity.User;
import com.trinity.ctc.domain.user.repository.UserRepository;
import com.trinity.ctc.global.exception.CustomException;
import com.trinity.ctc.global.exception.error_code.ReservationErrorCode;
import com.trinity.ctc.global.exception.error_code.UserErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
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

import static com.trinity.ctc.domain.notification.formatter.NotificationFormatter.formattingDailyNotification;
import static com.trinity.ctc.domain.notification.formatter.NotificationFormatter.formattingHourBeforeNotification;
import static com.trinity.ctc.domain.notification.formatter.NotificationHistoryFormatter.formattingMultipleNotificationHistory;
import static com.trinity.ctc.domain.notification.formatter.NotificationMessageFormatter.createMessageWithUrl;
import static com.trinity.ctc.domain.notification.type.NotificationType.BEFORE_ONE_HOUR_NOTIFICATION;
import static com.trinity.ctc.domain.notification.type.NotificationType.DAILY_NOTIFICATION;
import static com.trinity.ctc.global.util.formatter.DateTimeUtil.combineWithToday;

@Slf4j
@EnableAsync
@Service
@RequiredArgsConstructor
public class ReservationNotificationService {
    private final UserRepository userRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationNotificationRepository reservationNotificationRepository;
    private final NotificationSenderV1 notificationSenderV1;
    private final NotificationHistoryService notificationHistoryService;

    // 알림 목록 조회 시, 한 번에 가져오는 slice의 크기
    private final int SLICES_PER_PAGE = 5000;
    // 발송할 알림의 batch-size(Firebase Messaging service 에서 send 메서드의 요청으로 보낼 수 있는 최대 건수)
    private final int BATCH_SIZE = 500;

    /**
     * 예약 이벤트 발생 시, 예약 확인 알림(당일 알림, 1시간 전 알림)을 포멧팅하여 DB에 저장하는 메서드
     *
     * @param userId        사용자 ID
     * @param reservationId 예약 정보 ID
     */
    @Transactional
    public void registerReservationNotification(long userId, long reservationId) {
        // 사용자 조회
        User user = userRepository.findById(userId).orElseThrow(() -> new CustomException(UserErrorCode.NOT_FOUND));
        // 예약 정보 조회
        Reservation reservation = reservationRepository.findById(reservationId).orElseThrow(() -> new CustomException(ReservationErrorCode.NOT_FOUND));
        // 당일 예약 알림 메세지 포멧팅
        ReservationNotification dailyNotification = formattingDailyNotification(user, reservation);
        // 예약 1시간 전 알림 메세지 포멧팅
        ReservationNotification hourBeforeNotification = formattingHourBeforeNotification(user, reservation);
        // 알림 2가지 예약 알림 table 에 저장
        List<ReservationNotification> notificationList = new ArrayList<>(Arrays.asList(dailyNotification, hourBeforeNotification));
        reservationNotificationRepository.saveAll(notificationList);
    }

    /**
     * 예약 취소 시, 해당 예약에 대한 알림 data 를 삭제하는 메서드
     *
     * @param reservationId 예약 ID
     */
    @Transactional
    public void deleteReservationNotification(Long reservationId) {
        reservationNotificationRepository.deleteAllByReservationId(reservationId);
    }

    /**
     * 매일 8시에 당일 예약 알림을 보내는 로직을 처리하는 메서드
     *
     * @param scheduledDate 당일 날짜(yyyy-MM-dd)
     */
    public void sendDailyNotification(LocalDate scheduledDate) {
        log.info("✅✅✅ 당일 예약 알림 처리 시작!");
        long startTime = System.nanoTime(); // 시작 시간 측정

        int page = 0;
        Slice<ReservationNotification> slice;

        // slice-size 별로 발송 처리
        do {
            Pageable pageable = PageRequest.of(page, SLICES_PER_PAGE);
            // 알림 타입과 오늘 날짜로 당일 예약 알림 정보 가져오기
            slice = reservationNotificationRepository
                    .findSliceByTypeAndScheduledDate(DAILY_NOTIFICATION, scheduledDate, pageable);

            List<ReservationNotification> reservationNotificationList = slice.getContent();

            if (reservationNotificationList.isEmpty()) break;

            reservationNotificationList.forEach(n -> {
                n.getUser().getFcmList();
            });

            sendNotification(reservationNotificationList, DAILY_NOTIFICATION);

            page++;
        } while (slice.hasNext());

        long endTimeToProcess = System.nanoTime();  // 종료 시간 측정
        long elapsedTimeToProcess = endTimeToProcess - startTime; // 발송 처리까지의 경과 시간
        log.info("✅✅✅ 당일 예약 알림 처리 시간: {} ms", elapsedTimeToProcess / 1_000_000);

        // 해당 날짜에 스케줄된 당일 예약 알림을 table에서 삭제하는 메서드 호출
        deleteDailyNReservationNotification();
    }

    /**
     * 예약 1시간 전 알림을 보내는 메서드
     *
     * @param scheduledTime 스케줄된 시간(yyyy-MM-dd HH:mm)
     */
    public void sendHourBeforeNotification(LocalDateTime scheduledTime) {
        log.info("✅✅✅ 한 시간 전 예약 알림 처리 시작!");
        long startTime = System.nanoTime(); // 시작 시간 측정

        int page = 0;
        Slice<ReservationNotification> slice;

        // slice-size 별로 발송 처리
        do {
            Pageable pageable = PageRequest.of(page, SLICES_PER_PAGE);
            slice = reservationNotificationRepository.findSliceByTypeAndScheduledTime(
                    BEFORE_ONE_HOUR_NOTIFICATION, scheduledTime, pageable
            );

            List<ReservationNotification> reservationNotificationList = slice.getContent();

            if (reservationNotificationList.isEmpty()) break;

            reservationNotificationList.forEach(n -> {
                n.getUser().getFcmList();
            });

            sendNotification(reservationNotificationList, BEFORE_ONE_HOUR_NOTIFICATION);

            page++;
        } while (slice.hasNext());

        long endTimeToProcess = System.nanoTime();  // 종료 시간 측정
        long elapsedTimeToProcess = endTimeToProcess - startTime; // 발송 처리까지의 경과 시간
        log.info("✅✅✅ 한 시간 전 예약 알림 처리 시간: {} ms", elapsedTimeToProcess / 1_000_000);

        // 해당 시간에 스케줄된 한시간 전 예약 알림을 table에서 삭제하는 메서드
        deleteHourlyNReservationNotification(scheduledTime);
    }

    /**
     * 예약 알림 별 발송할 Message 객체를 생성하고, batch-size 별로 발송하는 메서드
     *
     * @param reservationNotificationList 예약 알림 리스트
     * @param type                        알림 타입
     * @return 알림 history List를 비동기로 반환하는 CompletableFuture 객체 리스트
     */
    @Async("reservation-notification")
    public void sendNotification(List<ReservationNotification> reservationNotificationList, NotificationType type) {

        log.info("✅✅✅ 예약 알림 발송 시작!");
        long startTime = System.nanoTime(); // 시작 시간 측정

        // 반환 리스트 초기화
        List<CompletableFuture<List<NotificationHistory>>> resultList = new ArrayList<>();
        // FcmMessage 리스트 초기화
        List<FcmMessage> messageList = new ArrayList<>();

        // 예약 알림 리스트 내의 data 와 각 알림 별 수신자의 Fcm 토큰으로 FcmMessage 생성 -> 리스트에 추가
        for (ReservationNotification notification : reservationNotificationList) {
            for (Fcm fcm : notification.getUser().getFcmList()) {
                FcmMessage message = createMessageWithUrl(notification.getTitle(), notification.getBody(), notification.getUrl(), fcm, type);
                messageList.add(message);
            }
        }

        // batch-size(500)으로 발송할 메세지 리스트 파티셔닝
        List<List<FcmMessage>> batches = Lists.partition(messageList, BATCH_SIZE);

        // 파티셔닝된 배치 별로 발송 로직 수행
        for (List<FcmMessage> batch : batches) {
            // 어러 건의 알림 발송 메서드 호출
            CompletableFuture<List<NotificationHistory>> sendingResult = notificationSenderV1.sendEachNotification(batch)
                    // 비동기로 발송 결과와 fcm, 알림 타입에 맞춰 알림 history List 를 생성하는 메서드 호출 -> 발송 응답 수신 시, List<NotificationHistory>를 반환
                    .thenApplyAsync(resultDtoList -> formattingMultipleNotificationHistory(batch, resultDtoList));
            // 반환할 결과 리스트에 추가
            resultList.add(sendingResult);
        }

        long endTimeToSend = System.nanoTime();  // 종료 시간 측정
        long elapsedTimeToSend = endTimeToSend - startTime;  // 발송까지의 경과 시간
        log.info("✅✅✅ 예약 알림 발송 실행 시간: {} ms", elapsedTimeToSend / 1_000_000);

        // 전송 결과로 반환된 각 future 의 전달이 완료될 때까지 기다린 후 알림 history 리스트로 반환
        List<NotificationHistory> notificationHistoryList = resultList.stream()
                .map(CompletableFuture::join)
                .flatMap(List::stream) // 여러 리스트를 하나로 합침
                .toList();

        long endTimeToResponse = System.nanoTime();  // 종료 시간 측정
        long elapsedTimeToResponse = endTimeToResponse - startTime; // 응답 반환까지의 경과 시간
        log.info("✅✅✅ 예약 알림 응답 시간: {} ms", elapsedTimeToResponse / 1_000_000);

        // 전송된 알림의 히스토리를 전부 history 테이블에 저장하는 메서드 호출
        notificationHistoryService.saveNotificationHistory(notificationHistoryList);
    }

    /**
     * 발송한 당일 예약 알림을 삭제하는 내부 메서드
     */
    private void deleteDailyNReservationNotification() {
        LocalDateTime scheduledTime = combineWithToday(LocalTime.of(8, 0));
        reservationNotificationRepository.deleteAllByScheduledTimeAndType(scheduledTime, DAILY_NOTIFICATION);
    }

    /**
     * 발송한 1시간 전 예약 알림을 삭제하는 내부 메서드
     *
     * @param scheduledTime 발송 스케줄 시간
     */
    private void deleteHourlyNReservationNotification(LocalDateTime scheduledTime) {
        reservationNotificationRepository.deleteAllByScheduledTimeAndType(scheduledTime, BEFORE_ONE_HOUR_NOTIFICATION);
    }
}
