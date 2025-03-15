package com.trinity.ctc.domain.notification.sender;

import com.google.firebase.messaging.*;
import com.trinity.ctc.domain.notification.dto.FcmSendingResultDto;
import com.trinity.ctc.domain.notification.result.SentResult;
import com.trinity.ctc.global.exception.CustomException;
import com.trinity.ctc.global.exception.error_code.FcmErrorCode;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class NotificationSender {
    /**
     * 알림 전송 로직 중 FCM 메세지를 발송하는 내부 메서드
     *
     * @param messageList FCM 메세지 객체 리스트
     * @return
     */
    public List<FcmSendingResultDto> sendEachNotification(List<Message> messageList) {
        // FCM 메세지 전송 결과를 담는 DTO
        FcmSendingResultDto result;
        List<FcmSendingResultDto> resultList = new ArrayList<>();

        for (Message message : messageList) {
            try {
                // FCM 서버에 메세지 전송
                FirebaseMessaging.getInstance().send(message);
                // 전송 결과(전송 시간, 전송 상태)
                result = new FcmSendingResultDto(LocalDateTime.now(), SentResult.SUCCESS);
            } catch (FirebaseMessagingException e) {
                // 전송 결과(전송 시간, 전송 상태, 에러 코드)
                result = new FcmSendingResultDto(LocalDateTime.now(), SentResult.FAILED, e.getMessagingErrorCode());
            }
            resultList.add(result);
        }

        return resultList;
    }

    /**
     * MulticastMessage를 발송하는 내부 메서드
     *
     * @param message
     * @return
     */
    public List<FcmSendingResultDto> sendMulticastNotification(MulticastMessage message) {
        // FCM 메세지 전송 결과를 담는 DTO
        List<FcmSendingResultDto> resultList = new ArrayList<>();

        try {
            // FCM 서버에 메세지 전송
            List<SendResponse> sendResponseList = FirebaseMessaging.getInstance().sendEachForMulticast(message, true).getResponses();
            // 전송 결과(전송 시간, 전송 상태)
            for (SendResponse sendResponse : sendResponseList) {
                LocalDateTime time = LocalDateTime.now();

                if (sendResponse.isSuccessful()) {
                    resultList.add(new FcmSendingResultDto(time, SentResult.SUCCESS));
                } else {
                    resultList.add(new FcmSendingResultDto(time, SentResult.FAILED, sendResponse.getException().getMessagingErrorCode()));
                }
            }
        } catch (FirebaseMessagingException e) {
            // 전송 결과(전송 시간, 전송 상태, 에러 코드
            throw new CustomException(FcmErrorCode.SENDING_REQUEST_FAILED);
        }

        return resultList;
    }

    /**
     * 재발송 로직 구현!!!
     */
}
