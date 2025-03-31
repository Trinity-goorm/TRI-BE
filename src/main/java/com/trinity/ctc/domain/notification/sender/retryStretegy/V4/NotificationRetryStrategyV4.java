package com.trinity.ctc.domain.notification.sender.retryStretegy.V4;

import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.MessagingErrorCode;
import com.trinity.ctc.domain.notification.dto.FcmSendingResultDto;
import com.trinity.ctc.domain.notification.message.FcmMessage;
import com.trinity.ctc.domain.notification.result.SentResult;
import com.trinity.ctc.domain.notification.sender.retryStretegy.NotificationRetryStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
public class NotificationRetryStrategyV4 implements NotificationRetryStrategy {

    private final MessageChannel retryInputChannel;

    @Override
    public CompletableFuture<FcmSendingResultDto> retry(FirebaseMessagingException e, FcmMessage message) {
        MessagingErrorCode errorCode = e.getMessagingErrorCode();

        int retryCount;
        long retryDelay;
        switch (errorCode) {
            case UNAVAILABLE, INTERNAL -> {
                retryCount = 3;
                retryDelay = 10000 + (long) Math.pow(2, 0) * 1000;
            }
            case QUOTA_EXCEEDED -> {
                retryCount = 1;
                retryDelay = 60000;
            }
            default -> {
                return CompletableFuture.completedFuture(
                        new FcmSendingResultDto(LocalDateTime.now(), SentResult.FAILED, errorCode)
                );
            }
        }

        RetryMessageV4 retryMessage = new RetryMessageV4(message, retryCount, errorCode);
        retryInputChannel.send(MessageBuilder.withPayload(retryMessage).setHeader("delay", retryDelay).build());

        return CompletableFuture.completedFuture(
                new FcmSendingResultDto(LocalDateTime.now(), SentResult.FAILED, errorCode)
        );
    }


    @Override
    public int getStrategyVersion() {
        return 4;
    }
}
