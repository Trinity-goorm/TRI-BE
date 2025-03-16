package com.trinity.ctc.domain.notification.service;

import com.google.firebase.messaging.MessagingErrorCode;
import com.trinity.ctc.domain.fcm.entity.Fcm;
import com.trinity.ctc.domain.notification.dto.FcmMessageDto;
import com.trinity.ctc.domain.notification.dto.FcmMulticastMessageDto;
import com.trinity.ctc.domain.notification.dto.FcmSendingResultDto;
import com.trinity.ctc.domain.notification.entity.NotificationHistory;
import com.trinity.ctc.domain.notification.repository.NotificationHistoryRepository;
import com.trinity.ctc.domain.notification.result.SentResult;
import com.trinity.ctc.domain.notification.type.NotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
     * 알림 전송 로직 중 전송된 알림에 대한 히스토리를 빌드하는 내부 메서드
     *
     * @param messageDtoList FCM 메세지 정보 DTO 리스트
     * @param resultList     FCM 메세지 전송 결과 DTO 리스트
     * @param type           알림 타입
     * @return
     */
    public List<NotificationHistory> buildNotificationHistory(List<FcmMessageDto> messageDtoList,
                                                               List<FcmSendingResultDto> resultList, NotificationType type) {
        List<NotificationHistory> notificationHistoryList = new ArrayList<>();

        for (int i = 0; i < messageDtoList.size(); i++) {
            // 보낸 FCM 메세지를 JSON으로 저장하기 위해 Map 사용
            Map<String, String> messageHistory = new HashMap<>();
            messageHistory.put("title", messageDtoList.get(i).getTitle());
            messageHistory.put("body", messageDtoList.get(i).getBody());
            messageHistory.put("url", messageDtoList.get(i).getUrl());

            // 알림 history 빌드
            notificationHistoryList.add(createNotificationHistory(type, messageHistory, resultList.get(i).getSentAt(), resultList.get(i).getSentResult(),
                    resultList.get(i).getErrorCode(), messageDtoList.get(i).getFcmToken(), messageDtoList.get(i).getUser()));
        }

        return notificationHistoryList;
    }

    /**
     * Multicast Message의 알림 history 데이터를 build 하는 메서드
     *
     * @param multicastMessageDtoList
     * @param resultList
     * @param type
     * @return
     */
    public List<NotificationHistory> buildMulticastNotificationHistory(List<FcmMulticastMessageDto> multicastMessageDtoList,
                                                                       List<FcmSendingResultDto> resultList, NotificationType type) {
        List<NotificationHistory> notificationHistoryList = new ArrayList<>();

        for (int i = 0; i < multicastMessageDtoList.size(); i++) {
            // 보낸 FCM 메세지를 JSON으로 저장하기 위해 Map 사용
            Map<String, String> messageHistory = new HashMap<>();
            messageHistory.put("title", multicastMessageDtoList.get(i).getTitle());
            messageHistory.put("body", multicastMessageDtoList.get(i).getBody());
            messageHistory.put("url", multicastMessageDtoList.get(i).getUrl());

            // 각 메시지에 대해 여러 개의 FCM 토큰을 개별적으로 처리
            List<Fcm> fcmTokens = multicastMessageDtoList.get(i).getFcmTokens();

            LocalDateTime sentAt = resultList.get(i).getSentAt();
            SentResult sentResult = resultList.get(i).getSentResult();
            MessagingErrorCode errorCode = resultList.get(i).getErrorCode();

            for (Fcm fcm : fcmTokens) {
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
    public void saveNotificationHistory(List<NotificationHistory> notificationHistoryList) {
        notificationHistoryRepository.saveAll(notificationHistoryList);
    }
}
