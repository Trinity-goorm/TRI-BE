package com.trinity.ctc.domain.notification.formatter;

import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;
import com.trinity.ctc.domain.fcm.entity.Fcm;
import com.trinity.ctc.domain.notification.message.FcmMessage;
import com.trinity.ctc.domain.notification.message.FcmMulticastMessage;
import com.trinity.ctc.domain.notification.type.NotificationType;
import com.trinity.ctc.domain.reservation.entity.Reservation;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.trinity.ctc.domain.notification.formatter.NotificationContentFormatter.*;

// FCM 에서 제공하는 Message, MulticastMessage 의 Wrapper 객체(FcmMessage, FcmMulticastMessage)를 생성하는 Util
public class NotificationMessageFormatter {

    public static FcmMessage createMessageWithUrl(String title, String body, String url, Fcm fcm, NotificationType type) {
        Message message = Message.builder()
//                .putData("title", title)
//                .putData("body", body)
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build())
                .putData("url", url)
                .setToken(fcm.getToken())
                .build();

        Map<String, String> data = new HashMap<>();
        data.put("title", title);
        data.put("body", body);
        data.put("url", url);

        return new FcmMessage(message, fcm, data, type);
    }

    public static FcmMulticastMessage createMulticastMessageWithUrl(String title, String body, String url, List<Fcm> fcmList, NotificationType type) {
        MulticastMessage multicastMessage = MulticastMessage.builder()
//                .putData("title", title)
//                .putData("body", body)
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build())
                .putData("url", url)
                .addAllTokens(fcmList.stream().map(Fcm::getToken).collect(Collectors.toList()))
                .build();

        Map<String, String> data = new HashMap<>();
        data.put("title", title);
        data.put("body", body);
        data.put("url", url);

        return new FcmMulticastMessage(multicastMessage, fcmList, data, type);
    }

    // 예약 완료 알림 메세지를 포멧팅하는 메서드
    // 예약 완료 알림은 바로 발송하여 저장하지 않기 때문에 FcmMessage 객체로 반환
    public static FcmMessage formattingReservationCompletedNotification(Fcm fcm, Reservation reservation, NotificationType type) {
        // 예약 완료 알림 메세지에 필요한 정보 변수 선언
        String restaurantName = reservation.getRestaurant().getName();
        LocalDate reservedDate = reservation.getReservationDate();
        LocalTime reservedTime = reservation.getReservationTime().getTimeSlot();
        int minCapacity = reservation.getSeatType().getMinCapacity();
        int maxCapacity = reservation.getSeatType().getMaxCapacity();

        // 알림 메세지 data 별 포멧팅 -> NotificationContentUtil 내의 각 formatting 메서드 호출
        String title = formatReservationCompletedNotificationTitle(restaurantName);
        String body = formatReservationCompletedNotificationBody(reservedDate, reservedTime, minCapacity, maxCapacity);
        String url = formatReservationNotificationUrl();

        // reservationNotification entity 를 생성하는 팩토리 메서드 호출 -> reservationNotification 반환
        return createMessageWithUrl(title, body, url, fcm, type);
    }

    // 예약 취소 메세지를 포멧팅하는 메서드
    // 예약 취소 알림은 바로 발송하여 저장하지 않기 때문에 FcmMessage 객체로 반환
    public static FcmMessage formattingReservationCanceledNotification(Fcm fcm, Reservation reservation, boolean isCODPassed, NotificationType type) {
        // 예약 완료 알림 메세지에 필요한 정보 변수 선언
        String restaurantName = reservation.getRestaurant().getName();
        LocalDate reservedDate = reservation.getReservationDate();
        LocalTime reservedTime = reservation.getReservationTime().getTimeSlot();

        // 알림 메세지 data 별 포멧팅 -> NotificationContentUtil 내의 각 formatting 메서드 호출
        String title;
        String body;
        // isCODPassed 에 따라 다른 title 과 body data 포멧팅
        if (isCODPassed) {
            title = formatReservationFullCanceledNotificationTitle(restaurantName);
            body = formatReservationFullCanceledNotificationBody(reservedDate, reservedTime, fcm.getUser().getEmptyTicketCount());
        } else {
            title = formatReservationNullCanceledNotificationTitle(restaurantName);
            body = formatReservationNullCanceledNotificationBody(reservedDate, reservedTime, fcm.getUser().getEmptyTicketCount());
        }

        String url = formatReservationNotificationUrl();

        // 알림 메세지 data 로 FcmMessage 객체를 생성하는 메서드 호출
        return createMessageWithUrl(title, body, url, fcm, type);
    }
}
