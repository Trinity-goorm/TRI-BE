package com.trinity.ctc.domain.notification.sender.retryStretegy.V4;

import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.messaging.Message;

@MessagingGateway
public interface RetryGateway {
    @Gateway(requestChannel = "retryInputChannel")
    void retry(Message<RetryMessageV4> message);
}