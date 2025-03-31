package com.trinity.ctc.domain.notification.sender.retryStretegy.V3;

import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.MessagingErrorCode;
import com.trinity.ctc.domain.notification.dto.FcmSendingResultDto;
import com.trinity.ctc.domain.notification.message.FcmMessage;
import com.trinity.ctc.domain.notification.result.SentResult;
import com.trinity.ctc.domain.notification.sender.retryStretegy.NotificationRetryStrategy;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

@Component
@AllArgsConstructor
public class NotificationRetryStrategyV3 implements NotificationRetryStrategy {

    private final RetryDelayQueueProcessor delayQueueProcessor;

    /**
     * FcmException 에 따라 재전송/실패 처리를 판단하는 메서드
     * 전송 실패 시, 모든 기록 저장
     * @param e       FcmException
     * @param message 재전송할 메세지
     * @return 전송 결과 DTO
     */
    @Override
    public CompletableFuture<FcmSendingResultDto> retry(FirebaseMessagingException e, FcmMessage message) {
        // Firebase Messaging Error 를 get
        MessagingErrorCode errorCode = e.getMessagingErrorCode();

        switch (errorCode) {
            // 500, 503에 해당할 경우
            case UNAVAILABLE, INTERNAL -> {
                delayQueueProcessor.enqueue(message, 3, 10000);
            }
            // 429에 해당할 경우
            case QUOTA_EXCEEDED -> {
                delayQueueProcessor.enqueue(message, 1, 50000);

            }
        }
        return CompletableFuture.completedFuture(new FcmSendingResultDto(LocalDateTime.now(), SentResult.FAILED, errorCode));
    }

    @Override
    public int getStrategyVersion() {
        return 3;
    }
}
