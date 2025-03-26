package com.trinity.ctc.domain.notification.formatter;

import com.trinity.ctc.domain.notification.dto.FcmSendingResultDto;
import com.trinity.ctc.domain.notification.entity.NotificationHistory;
import com.trinity.ctc.domain.notification.message.FcmMessage;
import com.trinity.ctc.domain.notification.message.FcmMulticastMessage;
import com.trinity.ctc.domain.notification.type.NotificationType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.trinity.ctc.domain.notification.entity.NotificationHistory.createNotificationHistory;

// 발송 알림 종류 별 알림 history entity 들을 반환하는 로직을 구현한 factory class
public class NotificationHistoryFormatter {

    // 단 건 알림에 대한 notificationHistory formatter
    public static NotificationHistory formattingSingleNotificationHistory(FcmMessage message,
                                                              FcmSendingResultDto result, NotificationType type) {

        // 발송한 알림 data 를 Json 으로 저장하기 위해 Map 에 저장
        Map<String, String> messageHistory = new HashMap<>();
        messageHistory.put("title", message.getData().get("title"));
        messageHistory.put("body", message.getData().get("body"));
        messageHistory.put("url", message.getData().get("url"));

        // notificationHistory entity 를 생성하는 factory 메서드 호출 -> notificationHistory 반환
        return createNotificationHistory(
                type,
                messageHistory,
                result.getSentAt(),
                result.getSentResult(),
                result.getErrorCode(),
                message.getFcm().getToken(),
                message.getFcm().getUser()
        );
    }

    // 여러 건의 알림에 대한 notificationHistory formatter
    public static List<NotificationHistory> formattingMultipleNotificationHistory(List<FcmMessage> messageList,
                                                                      List<FcmSendingResultDto> resultList, NotificationType type) {
        // notificationHistoryList 초기화
        List<NotificationHistory> notificationHistoryList = new ArrayList<>();

        // 발송한 모든 알림을 notificationHistory 로 생성하여 list 에 저장
        for (int i = 0; i < messageList.size(); i++) {
            // 발송한 알림 data 를 Json 으로 저장하기 위해 Map 에 저장
            Map<String, String> messageHistory = new HashMap<>();
            messageHistory.put("title", messageList.get(i).getData().get("title"));
            messageHistory.put("body", messageList.get(i).getData().get("body"));
            messageHistory.put("url", messageList.get(i).getData().get("url"));

            // notificationHistory entity 를 생성하는 factory 메서드 호출 -> 반환된 notificationHistory 을 list 에 저장
            notificationHistoryList.add(createNotificationHistory(
                    type,
                    messageHistory,
                    resultList.get(i).getSentAt(),
                    resultList.get(i).getSentResult(),
                    resultList.get(i).getErrorCode(),
                    messageList.get(i).getFcm().getToken(),
                    messageList.get(i).getFcm().getUser()
            ));
        }

        // notificationHistoryList 반환
        return notificationHistoryList;
    }

    // Multicast 알림에 대한 notificationHistory formatter
    public static List<NotificationHistory> formattingMulticastNotificationHistory(FcmMulticastMessage multicastMessage,
                                                                       List<FcmSendingResultDto> resultList, NotificationType type) {
        // notificationHistoryList 초기화
        List<NotificationHistory> notificationHistoryList = new ArrayList<>();

        // 발송한 MulticastMessage 의 전송 결과를 notificationHistory 로 생성하여 list 에 저장
        for (int i = 0; i < multicastMessage.getFcmList().size(); i++) {
            // 발송한 알림 data 를 Json 으로 저장하기 위해 Map 에 저장
            Map<String, String> messageHistory = new HashMap<>();
            messageHistory.put("title", multicastMessage.getData().get("title"));
            messageHistory.put("body", multicastMessage.getData().get("body"));
            messageHistory.put("url", multicastMessage.getData().get("url"));

            // notificationHistory entity 를 생성하는 factory 메서드 호출 -> 반환된 notificationHistory 을 list 에 저장
            notificationHistoryList.add(createNotificationHistory(
                    type,
                    messageHistory,
                    resultList.get(i).getSentAt(),
                    resultList.get(i).getSentResult(),
                    resultList.get(i).getErrorCode(),
                    multicastMessage.getFcmList().get(i).getToken(),
                    multicastMessage.getFcmList().get(i).getUser()
            ));
        }

        // notificationHistoryList 반환
        return notificationHistoryList;
    }
}
