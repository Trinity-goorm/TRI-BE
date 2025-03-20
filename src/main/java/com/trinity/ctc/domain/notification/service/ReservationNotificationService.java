package com.trinity.ctc.domain.notification.service;

import com.google.firebase.messaging.Message;
import com.trinity.ctc.domain.fcm.repository.FcmRepository;
import com.trinity.ctc.domain.notification.dto.FcmMessageDto;
import com.trinity.ctc.domain.notification.dto.FcmSendingResultDto;
import com.trinity.ctc.domain.notification.dto.GroupFcmInformationDto;
import com.trinity.ctc.domain.notification.entity.NotificationHistory;
import com.trinity.ctc.domain.notification.entity.ReservationNotification;
import com.trinity.ctc.domain.notification.fomatter.NotificationContentUtil;
import com.trinity.ctc.domain.notification.repository.ReservationNotificationRepository;
import com.trinity.ctc.domain.notification.sender.NotificationSender;
import com.trinity.ctc.domain.notification.type.NotificationType;
import com.trinity.ctc.domain.reservation.entity.Reservation;
import com.trinity.ctc.domain.reservation.repository.ReservationRepository;
import com.trinity.ctc.domain.user.entity.User;
import com.trinity.ctc.domain.user.repository.UserRepository;
import com.trinity.ctc.global.exception.CustomException;
import com.trinity.ctc.global.exception.error_code.FcmErrorCode;
import com.trinity.ctc.global.exception.error_code.ReservationErrorCode;
import com.trinity.ctc.global.exception.error_code.UserErrorCode;
import com.trinity.ctc.global.util.formatter.DateTimeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.trinity.ctc.domain.notification.entity.ReservationNotification.createReservationNotification;
import static com.trinity.ctc.domain.notification.fomatter.NotificationMessageUtil.createMessageWithUrl;
import static com.trinity.ctc.domain.notification.type.NotificationType.BEFORE_ONE_HOUR_NOTIFICATION;
import static com.trinity.ctc.domain.notification.type.NotificationType.DAILY_NOTIFICATION;
import static com.trinity.ctc.global.util.formatter.DateTimeUtil.combineWithDate;

@Slf4j
@EnableAsync
@Service
@RequiredArgsConstructor
public class ReservationNotificationService {
    private final UserRepository userRepository;
    private final FcmRepository fcmRepository;
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
        reservationNotificationRepository.deleteAllByReservation(reservationId);
    }

    /**
     * 매일 8시에 당일 예약 알림을 보내는 메서드
     */
    public void sendDailyNotification(LocalDate today) {

        // 알림 타입과 오늘 날짜로 당일 예약 알림 정보 가져오기
        List<ReservationNotification> reservationNotificationList = reservationNotificationRepository
                .findAllByTypeAndDate(DAILY_NOTIFICATION, today);
        if (reservationNotificationList.isEmpty()) return;

        // history 테이블과 알림 발송 후 알림 메세지 삭제를 위한 알림 ID 담을 list 세팅
        List<NotificationHistory> notificationHistoryList = new ArrayList<>();
        List<Long> reservationNotificationIdList = new ArrayList<>();

        // 전송할 알림 리스트를 전부 도는 알림 발송 로직(현재 동기 처리 중)
        for (ReservationNotification notification : reservationNotificationList) {
            // 단 건의 알림 전송 로직에 대해 처리하는 메서드
            List<NotificationHistory> notificationHistory = handleEachNotification(notification, DAILY_NOTIFICATION);
            notificationHistoryList.addAll(notificationHistory);
            reservationNotificationIdList.add(notification.getId());
        }
        // 전송된 알림 히스토리를 전부 history 테이블에 저장하는 메서드
        notificationHistoryService.saveNotificationHistory(notificationHistoryList);
        // 전송한 예약 알림을 table에서 삭제하는 메서드
        deleteSentReservationNotification(reservationNotificationIdList);
    }

    /**
     * 예약 1시간 전 알림을 보내는 메서드
     */
    public void sendHourBeforeNotification(LocalDateTime now) {
        // 알림 타입과 현재 시간으로 보낼 예약 1시간 전 알림 정보 가져오기
        List<ReservationNotification> reservationNotificationList = reservationNotificationRepository
                .findAllByTypeAndDateTime(BEFORE_ONE_HOUR_NOTIFICATION, now);

        // history 테이블과 알림 발송 후 알림 메세지 삭제를 위한 알림 ID 담을 list 세팅
        List<NotificationHistory> notificationHistoryList = new ArrayList<>();
        List<Long> reservationNotificationIdList = new ArrayList<>();

        // 전송할 알림 리스트를 전부 도는 알림 발송 로직(현재 동기 처리 중)
        for (ReservationNotification notification : reservationNotificationList) {
            List<NotificationHistory> notificationHistory = handleEachNotification(notification, BEFORE_ONE_HOUR_NOTIFICATION);
            notificationHistoryList.addAll(notificationHistory);
            reservationNotificationIdList.add(notification.getId());
        }
        // 전송된 알림 히스토리를 전부 history 테이블에 저장하는 메서드
        notificationHistoryService.saveNotificationHistory(notificationHistoryList);
        // 전송한 예약 알림을 table에서 삭제하는 메서드
        deleteSentReservationNotification(reservationNotificationIdList);
    }

    /**
     * 단 건의 알림 전송 로직에 대해 처리하는 메서드
     *
     * @param notification 예약 알림 Entity
     * @param type         알림 타입(ENUM)
     * @return
     */
    private List<NotificationHistory> handleEachNotification(ReservationNotification notification, NotificationType type) {
        // 보낼 FCM 메세지 빌드
        GroupFcmInformationDto fcmInformationDto = buildReservationNotification(notification);
        // FCM 메세지 전송 및 전송 결과 반환
        List<FcmSendingResultDto> resultList = notificationSender.sendNotification(fcmInformationDto.getMessageList());
        return notificationHistoryService.buildNotificationHistory(fcmInformationDto.getMessageDtoList(), resultList, type);
    }

    /**
     * 알림 전송 로직 중 FCM 메세지를 빌드하는 내부 메서드
     *
     * @param notification 예약 알림 Entity
     * @return
     */
    private GroupFcmInformationDto buildReservationNotification(ReservationNotification notification) {
        // FCM 토큰 가져오기
        List<String> tokenList = fcmRepository.findByUser(notification.getUser().getId()).orElseThrow(() -> new CustomException(FcmErrorCode.NO_FCM_TOKEN_REGISTERED));

        List<Message> messageList = new ArrayList<>();
        List<FcmMessageDto> messageDtoList = new ArrayList<>();
        // FCM 메시지 빌드

        for (String token : tokenList) {
            messageList.add(createMessageWithUrl(notification.getTitle(), notification.getBody(), notification.getUrl(), token));
            messageDtoList.add(new FcmMessageDto(token, notification.getTitle(), notification.getBody(), notification.getUrl(), notification.getUser()));
        }

        return new GroupFcmInformationDto(messageDtoList, messageList);
    }

    /**
     * 전송한 예약 알림을 table에서 삭제하는 메서드
     *
     * @param reservationNotificationIdList
     */
    private void deleteSentReservationNotification(List<Long> reservationNotificationIdList) {
        reservationNotificationRepository.deleteAllById(reservationNotificationIdList);
    }
}
