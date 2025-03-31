package com.trinity.ctc.domain.notification.sender.retryStretegy.V3;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.MessagingErrorCode;
import com.trinity.ctc.domain.notification.dto.FcmSendingResultDto;
import com.trinity.ctc.domain.notification.message.FcmMessage;
import com.trinity.ctc.domain.notification.result.SentResult;
import com.trinity.ctc.domain.notification.service.NotificationHistoryService;
import com.trinity.ctc.global.exception.CustomException;
import com.trinity.ctc.global.exception.error_code.FcmErrorCode;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.DelayQueue;

import static com.trinity.ctc.domain.notification.formatter.NotificationHistoryFormatter.formattingSingleNotificationHistory;

@Slf4j
@Component
@RequiredArgsConstructor
public class RetryDelayQueueProcessor {

    private static final int RETRY_DELAY = 10000;

    private final NotificationHistoryService notificationHistoryService;
    private final DelayQueue<RetryMessageV3> queue = new DelayQueue<>();

    @PostConstruct
    public void init() {
        Thread.ofVirtual()
                .name("retry-queue-consumer-", 0)
                .start(this::consume);
    }

    public void enqueue(FcmMessage message, int retryCount, long delayMillis) {
        if (retryCount <= 0) return;
        queue.put(new RetryMessageV3(message, retryCount, delayMillis + RETRY_DELAY));
    }

    private void consume() {
        while (true) {
            try {
                RetryMessageV3 task = queue.take();
                FcmMessage message = task.getFcmMessage();
                int retryCount = task.getRetryCount();

                try {
                    FirebaseMessaging.getInstance().sendAsync(message.getMessage()).get();
                    FcmSendingResultDto resultDto = new FcmSendingResultDto(LocalDateTime.now(), SentResult.SUCCESS);
                    notificationHistoryService.saveSingleNotificationHistory(
                            formattingSingleNotificationHistory(message, resultDto));
                } catch (Exception e) {
                    if (e.getCause() instanceof FirebaseMessagingException fcmException) {
                        MessagingErrorCode errorCode = fcmException.getMessagingErrorCode();

                        if (errorCode == MessagingErrorCode.UNAVAILABLE || errorCode == MessagingErrorCode.INTERNAL) {
                            int nextRetryCount = retryCount - 1;
                            long delayMillis = (long) Math.pow(2, 3 - nextRetryCount) * 1000;
                            enqueue(message, nextRetryCount, delayMillis + RETRY_DELAY);
                        }

                        FcmSendingResultDto failResult = new FcmSendingResultDto(LocalDateTime.now(), SentResult.FAILED, errorCode);
                        notificationHistoryService.saveSingleNotificationHistory(
                                formattingSingleNotificationHistory(message, failResult));
                    } else {
                        // TODO: 예외 처리 로직 개선 필요 -> FirebaseMessagingException 이외의 에러 핸들링
                        log.error("❌ 처리되지 않은 에러: ", e);
                        // 현재는 FirebaseMessagingException 이외의 Exception 에 대해서 일괄 전송 실패 요청 에러
                        throw new CustomException(FcmErrorCode.SENDING_REQUEST_FAILED);
                    }
                }
            } catch (Exception ex) {
                // TODO: 예외 처리 로직 개선 필요 -> DLQueue 에러 핸들링
                log.error("❌ 소비 중 예외 발생", ex);
                // 현재는 FirebaseMessagingException 이외의 Exception 에 대해서 일괄 전송 실패 요청 에러
                throw new CustomException(FcmErrorCode.SENDING_REQUEST_FAILED);
            }
        }
    }
}
