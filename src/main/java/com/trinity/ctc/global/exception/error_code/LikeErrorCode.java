package com.trinity.ctc.global.exception.error_code;

import org.springframework.http.HttpStatus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LikeErrorCode implements ErrorCode {

    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 유저를 찾을 수 없습니다."),
    RESTAURANT_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 식당을 찾을 수 없습니다."),
    ALREADY_LIKED(HttpStatus.CONFLICT, "이미 찜한 식당입니다."),
    NOT_LIKED(HttpStatus.BAD_REQUEST, "찜하지 않은 식당입니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
