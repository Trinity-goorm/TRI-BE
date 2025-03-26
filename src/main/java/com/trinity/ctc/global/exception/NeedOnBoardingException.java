package com.trinity.ctc.global.exception;

import org.springframework.security.core.AuthenticationException;

public class NeedOnBoardingException extends AuthenticationException {
    public NeedOnBoardingException(String message) {
        super(message);
    }
}
