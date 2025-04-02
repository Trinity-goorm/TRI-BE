package com.trinity.ctc.domain.notification.sender.retryStretegy.V4;

import com.google.firebase.messaging.MessagingErrorCode;
import com.trinity.ctc.domain.notification.message.FcmMessage;

public record RetryMessageV4(FcmMessage message, int retryCount, MessagingErrorCode errorCode) {

}
