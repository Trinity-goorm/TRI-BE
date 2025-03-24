package com.trinity.ctc.domain.notification.service;

import com.trinity.ctc.domain.fcm.entity.Fcm;
import com.trinity.ctc.domain.notification.entity.NotificationHistory;
import com.trinity.ctc.domain.notification.message.FcmMessage;
import com.trinity.ctc.domain.notification.sender.NotificationSender;
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
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.trinity.ctc.domain.notification.fomatter.NotificationContentUtil.*;
import static com.trinity.ctc.domain.notification.fomatter.NotificationMessageUtil.createMessageWithUrl;
import static com.trinity.ctc.domain.notification.type.NotificationType.RESERVATION_COMPLETED;

@Slf4j
@EnableAsync
@Service
@RequiredArgsConstructor
public class ConfirmationNotificationService {
    private final UserRepository userRepository;
    private final ReservationRepository reservationRepository;
    private final NotificationHistoryService notificationHistoryService;
    private final NotificationSender notificationSender;

    /**
     * 예약 완료 알림 전송 메서드
     *
     * @param userId
     * @param reservationId
     */
    @Transactional(readOnly = true)
    public void sendReservationCompletedNotification(Long userId, Long reservationId) {
        // 사용자 조회, 해당하는 사용자가 없으면 404 반환
        User user = userRepository.findById(userId).orElseThrow(() -> new CustomException(UserErrorCode.NOT_FOUND));
        // 예약 내역 조회, 해당하는 예약 내역이 없으면 404 반환
        Reservation reservation = reservationRepository.findById(reservationId).orElseThrow(() -> new CustomException(ReservationErrorCode.NOT_FOUND));

        List<Fcm> tokenList = user.getFcmList();

        List<CompletableFuture<NotificationHistory>> resultList = new ArrayList<>();

//        FcmMessageDto messageData = formattingReservationCompletedNotification(reservation);
        for (Fcm fcm : tokenList) {
            FcmMessage message = formattingReservationCompletedNotification(fcm, reservation);
            resultList.add(sendConfirmationNotification(message, RESERVATION_COMPLETED));
        }

        List<NotificationHistory> notificationHistoryList = resultList.stream()
                .map(CompletableFuture::join) // 각 future의 실행이 끝날 때까지 기다림
                .toList();

        notificationHistoryService.saveNotificationHistory(notificationHistoryList);
    }

    /**
     * 예약 완료 알림 메세지를 포멧팅하는 내부 메서드
     *
     * @param reservation
     * @return
     */
    private FcmMessage formattingReservationCompletedNotification(Fcm fcm, Reservation reservation) {
        // 예약 완료 알림 메세지에 필요한 정보 변수 선언
        String restaurantName = reservation.getRestaurant().getName();
        LocalDate reservedDate = reservation.getReservationDate();
        LocalTime reservedTime = reservation.getReservationTime().getTimeSlot();
        int minCapacity = reservation.getSeatType().getMinCapacity();
        int maxCapacity = reservation.getSeatType().getMaxCapacity();

        // 알림 메세지 data 별 포멧팅
        String title = formatReservationCompletedNotificationTitle(restaurantName);
        String body = formatReservationCompletedNotificationBody(reservedDate, reservedTime, minCapacity, maxCapacity);
        String url = formatReservationNotificationUrl();

        return createMessageWithUrl(title, body, url, fcm);
    }

    /**
     * 예약 취소 알림 전송 메서드
     *
     * @param userId
     * @param reservationId
     * @param isCODPassed
     */
    @Transactional
    public void sendReservationCanceledNotification(Long userId, Long reservationId, boolean isCODPassed) {

        // 사용자 조회, 해당하는 사용자가 없으면 404 반환
        User user = userRepository.findById(userId).orElseThrow(() -> new CustomException(UserErrorCode.NOT_FOUND));
        // 예약 내역 조회, 해당하는 예약 내역이 없으면 404 반환
        Reservation reservation = reservationRepository.findById(reservationId).orElseThrow(() -> new CustomException(ReservationErrorCode.NOT_FOUND));

        List<Fcm> tokenList = user.getFcmList();

        List<CompletableFuture<NotificationHistory>> resultList = new ArrayList<>();

        for (Fcm fcm : tokenList) {
            FcmMessage message = formattingReservationCanceledNotification(fcm, reservation, isCODPassed);;
            resultList.add(sendConfirmationNotification(message, RESERVATION_COMPLETED));
        }

        List<NotificationHistory> notificationHistoryList = resultList.stream()
                .map(CompletableFuture::join) // 각 future의 실행이 끝날 때까지 기다림
                .toList();

        notificationHistoryService.saveNotificationHistory(notificationHistoryList);
    }

    /**
     * 예약 취소 메세지를 포멧팅하는 내부 메서드
     *
     * @param fcm
     * @param reservation
     * @param isCODPassed
     * @return
     */
    private FcmMessage formattingReservationCanceledNotification(Fcm fcm, Reservation reservation, boolean isCODPassed) {
        // 예약 완료 알림 메세지에 필요한 정보 변수 선언
        String restaurantName = reservation.getRestaurant().getName();
        LocalDate reservedDate = reservation.getReservationDate();
        LocalTime reservedTime = reservation.getReservationTime().getTimeSlot();

        String title;
        String body;

        // 알림 메세지 data 별 포멧팅
        if (isCODPassed) {
            title = formatReservationFullCanceledNotificationTitle(restaurantName);
            body = formatReservationFullCanceledNotificationBody(reservedDate, reservedTime, fcm.getUser().getEmptyTicketCount());
        } else {
            title = formatReservationNullCanceledNotificationTitle(restaurantName);
            body = formatReservationNullCanceledNotificationBody(reservedDate, reservedTime, fcm.getUser().getEmptyTicketCount());
        }

        String url = formatReservationNotificationUrl();

        return createMessageWithUrl(title, body, url, fcm);
    }


    private CompletableFuture<NotificationHistory> sendConfirmationNotification(FcmMessage message, NotificationType type) {
        return notificationSender.sendSingleNotification(message)
                .thenApplyAsync(result -> notificationHistoryService.buildSingleNotificationHistory(message, result, type));
    }
}
