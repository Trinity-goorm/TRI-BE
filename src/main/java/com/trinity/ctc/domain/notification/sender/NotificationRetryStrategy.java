package com.trinity.ctc.domain.notification.sender;

import com.google.firebase.messaging.FirebaseMessagingException;
import com.trinity.ctc.domain.notification.dto.FcmSendingResultDto;
import com.trinity.ctc.domain.notification.message.FcmMessage;

import java.util.concurrent.CompletableFuture;

public interface NotificationRetryStrategy {
    CompletableFuture<FcmSendingResultDto> retry(FirebaseMessagingException exception, FcmMessage message, int retryCount);
    int getStrategyVersion();
}
