package com.trinity.ctc.domain.notification.dto;

import com.google.firebase.messaging.MessagingErrorCode;
import com.trinity.ctc.domain.notification.result.SentResult;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

// FCM에 발송한 알림에 대한 응답 결과 관련 DTO
@Getter
@AllArgsConstructor
public class FcmSendingResultDto {
    // 발송 시간
    LocalDateTime sentAt;

    // 발송 결과(SUCCESS, FAILED)
    SentResult sentResult;

    // FCM Messaging 에서 반환하는 Error Code(Enum)
    MessagingErrorCode errorCode;

    public FcmSendingResultDto(LocalDateTime sentAt, SentResult sentResult) {
        this.sentAt = sentAt;
        this.sentResult = sentResult;
    }
}
