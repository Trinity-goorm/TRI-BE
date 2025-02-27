package com.trinity.ctc.util.exception;

import com.trinity.ctc.util.exception.error_code.ErrorCode;
import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {
    private final ErrorCode errorCode;

    public CustomException(ErrorCode errorCode) {
        super(errorCode.getMessage()); // Exception의 message 필드에도 넣기
        this.errorCode = errorCode;
    }

    public CustomException(ErrorCode errorCode, String customMessage) {
        super(customMessage);
        this.errorCode = errorCode;
    }
}
