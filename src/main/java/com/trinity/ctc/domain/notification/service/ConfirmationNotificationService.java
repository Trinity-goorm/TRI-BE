package com.trinity.ctc.domain.notification.service;

import com.google.firebase.messaging.Message;
import com.trinity.ctc.domain.fcm.entity.Fcm;
import com.trinity.ctc.domain.notification.dto.FcmMessageDto;
import com.trinity.ctc.domain.notification.dto.FcmSendingResultDto;
import com.trinity.ctc.domain.notification.dto.GroupFcmInformationDto;
import com.trinity.ctc.domain.notification.entity.NotificationHistory;
import com.trinity.ctc.domain.notification.sender.NotificationSender;
import com.trinity.ctc.domain.reservation.entity.Reservation;
import com.trinity.ctc.domain.reservation.repository.ReservationRepository;
import com.trinity.ctc.domain.user.entity.User;
import com.trinity.ctc.domain.user.repository.UserRepository;
import com.trinity.ctc.global.exception.CustomException;
import com.trinity.ctc.global.exception.error_code.FcmErrorCode;
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

import static com.trinity.ctc.domain.notification.fomatter.NotificationContentUtil.*;
import static com.trinity.ctc.domain.notification.fomatter.NotificationMessageUtil.createSendingMessageWithUrl;
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
        FcmMessageDto messageData = formattingReservationCompletedNotification(reservation);

        sendConfirmationNotification(user, messageData);
    }

    /**
     * 예약 완료 알림 메세지를 포멧팅하는 내부 메서드
     *
     * @param reservation
     * @return
     */
    private FcmMessageDto formattingReservationCompletedNotification(Reservation reservation) {
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

        return new FcmMessageDto(title, body, url);
    }

    /**
     * FCM 메세지 리스트를 build하는 내부 메서드
     *
     * @param user
     * @param fcmMessageDto
     * @return
     */
    private GroupFcmInformationDto buildMessageList(User user, FcmMessageDto fcmMessageDto) {
        List<Fcm> tokenList = user.getFcmList();
        if (tokenList.isEmpty()) throw new CustomException(FcmErrorCode.NO_FCM_TOKEN_REGISTERED);

        List<Message> messageList = new ArrayList<>();
        List<FcmMessageDto> fcmMessageDtoList = new ArrayList<>();
        Message message;

        for (Fcm token : tokenList) {
            message = createSendingMessageWithUrl(fcmMessageDto.getTitle(), fcmMessageDto.getBody(), fcmMessageDto.getUrl(), token.getToken());
            messageList.add(message);

            fcmMessageDtoList.add(FcmMessageDto.of(fcmMessageDto, token.getToken(), user));
        }

        return new GroupFcmInformationDto(fcmMessageDtoList, messageList);
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
        User user = userRepository.findById(userId).orElseThrow(() -> new CustomException(UserErrorCode.NOT_FOUND));
        Reservation reservation = reservationRepository.findById(reservationId).orElseThrow(() -> new CustomException(ReservationErrorCode.NOT_FOUND));
        FcmMessageDto messageData = formattingReservationCanceledNotification(reservation, user, isCODPassed);

        sendConfirmationNotification(user, messageData);
    }

    /**
     *
     * @param user
     * @param messageData
     */
    private void sendConfirmationNotification(User user, FcmMessageDto messageData) {
        GroupFcmInformationDto groupFcmInformationDto = buildMessageList(user, messageData);

        List<Message> messageList = groupFcmInformationDto.getMessageList();
        List<FcmMessageDto> messageDtoList = groupFcmInformationDto.getMessageDtoList();

        // 단 건의 알림 전송 로직에 대해 처리하는 메서드
        List<FcmSendingResultDto> resultList = notificationSender.sendNotification(messageList);
        List<NotificationHistory> notificationHistoryList = notificationHistoryService.buildNotificationHistory(messageDtoList, resultList, RESERVATION_COMPLETED);

        // 전송된 알림 히스토리를 전부 history 테이블에 저장하는 메서드
        notificationHistoryService.saveNotificationHistory(notificationHistoryList);
    }

    /**
     * 예약 취소 메세지를 포멧팅하는 내부 메서드                                                         
     *
     * @param reservation
     * @param user
     * @param isCODPassed
     * @return
     */
    private FcmMessageDto formattingReservationCanceledNotification(Reservation reservation, User user, boolean isCODPassed) {
        // 예약 완료 알림 메세지에 필요한 정보 변수 선언
        String restaurantName = reservation.getRestaurant().getName();
        LocalDate reservedDate = reservation.getReservationDate();
        LocalTime reservedTime = reservation.getReservationTime().getTimeSlot();

        String title;
        String body;

        // 알림 메세지 data 별 포멧팅
        if (isCODPassed) {
            title = formatReservationFullCanceledNotificationTitle(restaurantName);
            body = formatReservationFullCanceledNotificationBody(reservedDate, reservedTime, user.getEmptyTicketCount());
        } else {
            title = formatReservationNullCanceledNotificationTitle(restaurantName);
            body = formatReservationNullCanceledNotificationBody(reservedDate, reservedTime, user.getEmptyTicketCount());
        }

        String url = formatReservationNotificationUrl();

        return new FcmMessageDto(title, body, url);
    }
}
