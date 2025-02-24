package com.trinity.ctc.util.exception.error_code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum FcmErrorCode implements ErrorCode {
    FIREBASE_INITIALIZATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Firebase 초기화에 실패했습니다."),
    SENDING_REQUEST_FAILED(HttpStatus.BAD_REQUEST, "FCM 메세지 전송 요청이 실패했습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
