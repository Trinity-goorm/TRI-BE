package com.trinity.ctc.util.exception;

import com.trinity.ctc.util.exception.error_code.ErrorCode;
import org.springframework.security.access.AccessDeniedException;

public class CustomAccessDeniedException extends AccessDeniedException {

    private final ErrorCode errorCode;

    public CustomAccessDeniedException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
