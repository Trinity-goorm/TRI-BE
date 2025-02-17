package com.trinity.ctc.util.exception.error_code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum FcmTokenErrorCode implements ErrorCode {
    TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자의 FCM 토큰이 존재하지 않습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
