package com.trinity.ctc.domain.notification.service;

import com.trinity.ctc.domain.notification.entity.ReservationNotification;
import com.trinity.ctc.domain.notification.entity.type.NotificationType;
import com.trinity.ctc.domain.notification.repository.ReservationNotificationRepository;
import com.trinity.ctc.domain.notification.util.NotificationMessageUtil;
import com.trinity.ctc.domain.reservation.entity.Reservation;
import com.trinity.ctc.domain.reservation.repository.ReservationRepository;
import com.trinity.ctc.domain.user.entity.User;
import com.trinity.ctc.event.ReservationCanceledEvent;
import com.trinity.ctc.event.ReservationSuccessEvent;
import com.trinity.ctc.kakao.repository.UserRepository;
import com.trinity.ctc.util.exception.CustomException;
import com.trinity.ctc.util.exception.error_code.ReservationErrorCode;
import com.trinity.ctc.util.exception.error_code.UserErrorCode;
import com.trinity.ctc.util.formatter.DateTimeUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final ReservationNotificationRepository reservationNotificationRepository;
    private final UserRepository userRepository;
    private final ReservationRepository reservationRepository;

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
    public ReservationNotification formattingDailyNotification(User user, Reservation reservation) {
        String userName = user.getNickname();
        String restaurantName = reservation.getRestaurant().getName();
        LocalDate reservedDate = reservation.getReservationDate();
        LocalTime reservedTime = reservation.getReservationTime().getTimeSlot();
        Long reservationId = reservation.getId();
        LocalDateTime scheduledTime = DateTimeUtil.combineWithDate(reservedDate, LocalTime.of(8, 0));

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

//    @Scheduled
    public void sendReservationNotification() {

    }
}
