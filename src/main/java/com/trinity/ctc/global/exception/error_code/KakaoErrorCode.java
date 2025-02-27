package com.trinity.ctc.global.exception.error_code;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum KakaoErrorCode implements ErrorCode {
    INVALID_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 액세스 토큰입니다."),
    LOGOUT_REQUEST_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "카카오 로그아웃 요청 실패"),
    NETWORK_ERROR(HttpStatus.SERVICE_UNAVAILABLE, "카카오 서버에 접근할 수 없습니다."),
    UNKNOWN_LOGOUT_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "카카오 로그아웃 중 알 수 없는 오류 발생");

    private final HttpStatus httpStatus;
    private final String message;

    KakaoErrorCode(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }
}
