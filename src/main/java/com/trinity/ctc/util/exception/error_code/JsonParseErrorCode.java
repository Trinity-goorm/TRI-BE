package com.trinity.ctc.util.exception.error_code;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum JsonParseErrorCode implements ErrorCode {
    ENTITY_CONVERT_FAILED(HttpStatus.BAD_REQUEST, "Entity 변환 실패"),
    COLUMN_CONVERT_FAILED(HttpStatus.BAD_REQUEST, "Json 컬럼 변환 실패"),
    FILE_READ_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "파일을 읽을 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
