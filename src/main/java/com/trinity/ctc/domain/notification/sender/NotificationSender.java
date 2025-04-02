package com.trinity.ctc.domain.notification.sender;

import com.trinity.ctc.domain.notification.dto.FcmSendingResultDto;
import com.trinity.ctc.domain.notification.message.FcmMessage;
import com.trinity.ctc.domain.notification.message.FcmMulticastMessage;

import java.util.List;
import java.util.concurrent.CompletableFuture;
public interface NotificationSender {

    CompletableFuture<FcmSendingResultDto> sendSingleNotification(FcmMessage message);

    CompletableFuture<List<FcmSendingResultDto>> sendEachNotification(List<FcmMessage> messageList);

    CompletableFuture<List<FcmSendingResultDto>> sendMulticastNotification(FcmMulticastMessage message);

    int getStrategyVersion();
}
