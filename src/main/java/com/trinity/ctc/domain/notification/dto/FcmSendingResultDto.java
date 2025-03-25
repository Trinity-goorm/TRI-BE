package com.trinity.ctc.domain.notification.dto;

import com.google.firebase.messaging.MessagingErrorCode;
import com.trinity.ctc.domain.notification.result.SentResult;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class FcmSendingResultDto {
    LocalDateTime sentAt;
    SentResult sentResult;

    // FCM Messaging 에서 반환하는 Error Code(Enum)
    MessagingErrorCode errorCode;

    public FcmSendingResultDto(LocalDateTime sentAt, SentResult sentResult) {
        this.sentAt = sentAt;
        this.sentResult = sentResult;
    }
}
