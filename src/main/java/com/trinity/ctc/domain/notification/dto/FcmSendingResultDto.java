package com.trinity.ctc.domain.notification.dto;

import com.google.firebase.messaging.MessagingErrorCode;
import com.trinity.ctc.domain.notification.entity.result.SentResult;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class FcmSendingResultDto {
    LocalDateTime sentAt;
    SentResult sentResult;
    MessagingErrorCode errorCode;

    public FcmSendingResultDto(LocalDateTime sentAt, SentResult sentResult) {
        this.sentAt = sentAt;
        this.sentResult = sentResult;
    }
}
