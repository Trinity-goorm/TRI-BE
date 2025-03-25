package com.trinity.ctc.domain.notification.service;

import com.google.firebase.messaging.MessagingErrorCode;
import com.trinity.ctc.domain.fcm.entity.Fcm;
import com.trinity.ctc.domain.notification.dto.FcmSendingResultDto;
import com.trinity.ctc.domain.notification.entity.NotificationHistory;
import com.trinity.ctc.domain.notification.message.FcmMessage;
import com.trinity.ctc.domain.notification.message.FcmMulticastMessage;
import com.trinity.ctc.domain.notification.repository.NotificationHistoryRepository;
import com.trinity.ctc.domain.notification.result.SentResult;
import com.trinity.ctc.domain.notification.type.NotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.trinity.ctc.domain.notification.entity.NotificationHistory.createNotificationHistory;
import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;

@Slf4j
@Service
@EnableAsync
@RequiredArgsConstructor
public class NotificationHistoryService {
    private final NotificationHistoryRepository notificationHistoryRepository;

    /**
     * 전송된 단 건의 알림에 대한 히스토리를 빌드하는 내부 메서드
     *
     * @param message 전송된 Message 에 대한 Wrapper 객체
     * @param result  전송 결과 Dto
     * @param type    전송된 알림 타입
     * @return 알림 History Entity
     */
    public NotificationHistory buildSingleNotificationHistory(FcmMessage message,
                                                              FcmSendingResultDto result, NotificationType type) {

        // data로 정제
        Map<String, String> messageHistory = new HashMap<>();
        messageHistory.put("title", message.getData().get("title"));
        messageHistory.put("body", message.getData().get("body"));
        messageHistory.put("url", message.getData().get("url"));

        return createNotificationHistory(type, messageHistory, result.getSentAt(), result.getSentResult(),
                result.getErrorCode(), message.getFcm().getToken(), message.getFcm().getUser());
    }


    /**
     * 알림 전송 로직 중 전송된 알림에 대한 히스토리를 빌드하는 내부 메서드
     *
     * @param messageList FCM 메세지 정보 DTO 리스트
     * @param resultList  FCM 메세지 전송 결과 DTO 리스트
     * @param type        알림 타입
     * @return
     */
    public List<NotificationHistory> buildMultipleNotificationHistory(List<FcmMessage> messageList,
                                                                      List<FcmSendingResultDto> resultList, NotificationType type) {
        List<NotificationHistory> notificationHistoryList = new ArrayList<>();

        for (int i = 0; i < messageList.size(); i++) {
            // 보낸 FCM 메세지를 JSON으로 저장하기 위해 Map 사용
            Map<String, String> messageHistory = new HashMap<>();
            messageHistory.put("title", messageList.get(i).getData().get("title"));
            messageHistory.put("body", messageList.get(i).getData().get("body"));
            messageHistory.put("url", messageList.get(i).getData().get("url"));

            // 알림 history 빌드
            notificationHistoryList.add(createNotificationHistory(type, messageHistory, resultList.get(i).getSentAt(), resultList.get(i).getSentResult(),
                    resultList.get(i).getErrorCode(), messageList.get(i).getFcm().getToken(), messageList.get(i).getFcm().getUser()));
        }

        return notificationHistoryList;
    }

    /**
     * Multicast Message의 알림 history 데이터를 build 하는 메서드
     *
     * @param multicastMessage
     * @param resultList
     * @param type
     * @return
     */
    public List<NotificationHistory> buildMulticastNotificationHistory(FcmMulticastMessage multicastMessage,
                                                                       List<FcmSendingResultDto> resultList, NotificationType type) {
        List<NotificationHistory> notificationHistoryList = new ArrayList<>();

        for (int i = 0; i < resultList.size(); i++) {
            // 보낸 FCM 메세지를 JSON으로 저장하기 위해 Map 사용
            Map<String, String> messageHistory = new HashMap<>();
            messageHistory.put("title", multicastMessage.getData().get("title"));
            messageHistory.put("body", multicastMessage.getData().get("body"));
            messageHistory.put("url", multicastMessage.getData().get("url"));

            // 각 메시지에 대해 여러 개의 FCM 토큰을 개별적으로 처리
            List<Fcm> fcmList = multicastMessage.getFcmList();

            LocalDateTime sentAt = resultList.get(i).getSentAt();
            SentResult sentResult = resultList.get(i).getSentResult();
            MessagingErrorCode errorCode = resultList.get(i).getErrorCode();

            for (Fcm fcm : fcmList) {
                notificationHistoryList.add(createNotificationHistory(
                        type,
                        messageHistory,
                        sentAt,
                        sentResult,
                        errorCode,
                        fcm.getToken(),
                        fcm.getUser()
                ));
            }
        }

        // 알림 history 빌드
        return notificationHistoryList;
    }

    /**
     * 전송된 알림 히스토리를 전부 history 테이블에 저장
     *
     * @param notificationHistoryList 알림 history Entity 리스트
     */
    @Transactional(propagation = REQUIRES_NEW)
    @Async("save-notification-history")
    public void saveNotificationHistory(List<NotificationHistory> notificationHistoryList) {
        log.info("저장 시작");
        notificationHistoryRepository.saveAll(notificationHistoryList);
        log.info("저장 끝");
    }
}
