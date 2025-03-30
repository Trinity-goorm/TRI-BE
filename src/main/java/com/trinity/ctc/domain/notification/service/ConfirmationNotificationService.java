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
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.trinity.ctc.domain.notification.formatter.NotificationHistoryFormatter.formattingSingleNotificationHistory;
import static com.trinity.ctc.domain.notification.formatter.NotificationMessageFormatter.formattingReservationCanceledNotification;
import static com.trinity.ctc.domain.notification.formatter.NotificationMessageFormatter.formattingReservationCompletedNotification;
import static com.trinity.ctc.domain.notification.type.NotificationType.RESERVATION_CANCELED;
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
     * @param userId 사용자 ID
     * @param reservationId 예약 ID
     */
    @Async("confirmation-notification")
    @Transactional(readOnly = true)
    public void sendReservationCompletedNotification(Long userId, Long reservationId) {
        // 사용자 조회, 해당하는 사용자가 없으면 404 반환
        User user = userRepository.findById(userId).orElseThrow(() -> new CustomException(UserErrorCode.NOT_FOUND));
        // 예약 내역 조회, 해당하는 예약 내역이 없으면 404 반환
        Reservation reservation = reservationRepository.findById(reservationId).orElseThrow(() -> new CustomException(ReservationErrorCode.NOT_FOUND));
        // 사용자의 FCM 토큰 리스트 조회
        List<Fcm> tokenList = user.getFcmList();
        // 응답 결과를 반환할 List 초기화 -> 응답 결과를 비동기로 받기 위해 Future 객체를 먼저 저장
        List<CompletableFuture<NotificationHistory>> resultList = new ArrayList<>();

        // 사용자의 FCM 토큰 별로 메세지 전송
        for (Fcm fcm : tokenList) {
            // 전송을 위한 Message 의 Wrapper 객체 포멧팅
            FcmMessage message = formattingReservationCompletedNotification(fcm, reservation);
            // 전송 메서드 호출 후 반환된 전송 응답을 list 에 저장
            resultList.add(sendSingleNotification(message, RESERVATION_COMPLETED));
        }

        // 전송 결과로 반환된 각 future 의 전달이 완료될 때까지 기다린 후 알림 history 리스트로 반환
        List<NotificationHistory> notificationHistoryList = resultList.stream()
                .map(CompletableFuture::join)
                .toList();

        // 알림 history 리스트를 저장하는 메서드 호출
        notificationHistoryService.saveNotificationHistory(notificationHistoryList);
    }

    /**
     * 예약 취소 알림 전송 메서드
     * @param userId 사용자 ID
     * @param reservationId 예약 ID
     * @param isCODPassed 예약시점에 따른 예약 비용 반환 여부(정책)
     */
    @Async("confirmation-notification")
    @Transactional(readOnly = true)
    public void sendReservationCanceledNotification(Long userId, Long reservationId, boolean isCODPassed) {

        // 사용자 조회, 해당하는 사용자가 없으면 404 반환
        User user = userRepository.findById(userId).orElseThrow(() -> new CustomException(UserErrorCode.NOT_FOUND));
        // 예약 내역 조회, 해당하는 예약 내역이 없으면 404 반환
        Reservation reservation = reservationRepository.findById(reservationId).orElseThrow(() -> new CustomException(ReservationErrorCode.NOT_FOUND));
        // 사용자의 FCM 토큰 리스트 조회
        List<Fcm> tokenList = user.getFcmList();
        // 응답 결과를 반환할 List 초기화 -> 응답 결과를 비동기로 받기 위해 Future 객체를 먼저 저장
        List<CompletableFuture<NotificationHistory>> resultList = new ArrayList<>();

        // 사용자의 FCM 토큰 별로 메세지 전송
        for (Fcm fcm : tokenList) {
            // 전송을 위한 Message 의 Wrapper 객체 포멧팅
            FcmMessage message = formattingReservationCanceledNotification(fcm, reservation, isCODPassed);
            // 전송 메서드 호출 후 반환된 전송 응답을 list 에 저장
            resultList.add(sendSingleNotification(message, RESERVATION_CANCELED));
        }

        // 전송 결과로 반환된 각 future 의 전달이 완료될 때까지 기다린 후 알림 history 리스트로 반환
        List<NotificationHistory> notificationHistoryList = resultList.stream()
                .map(CompletableFuture::join)
                .toList();

        // 알림 history 리스트를 저장하는 메서드 호출
        notificationHistoryService.saveNotificationHistory(notificationHistoryList);
    }

    /**
     * 단 건의 메세지 발송 메서드를 호출하고 반환된 결과를 처리하는 내부 메서드
     * @param message 발송할 Message 의 Wrapper 객체
     * @param type 알림 타입
     * @return 알림 history
     */
    private CompletableFuture<NotificationHistory> sendSingleNotification(FcmMessage message, NotificationType type) {

        // 단 건의 메세지를 발송하는 메서드 호출
        return notificationSender.sendSingleNotification(message)
                // 반환된 전송 결과 dto 와 Message data 로 알림 History 객체를 생성하는 메서드 호출
                .thenApplyAsync(result -> formattingSingleNotificationHistory(message, result, type));
    }
}
