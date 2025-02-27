package com.trinity.ctc.global.exception;

import com.trinity.ctc.global.exception.error_code.CommonErrorCode;
import com.trinity.ctc.global.exception.error_code.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    /* 사용자 지정 에러를 잡아서 처리 */
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<Object> handleCustomException(CustomException e) {
        ErrorCode errorCode = e.getErrorCode();
        return handleException(errorCode);
    }

    /* 이외의 모든 에러를 잡아서 처리 */
    @ExceptionHandler({Exception.class})
    public ResponseEntity<Object> handleException(Exception e) {

        CommonErrorCode errorCode;

        if (e instanceof IllegalArgumentException) {
            errorCode = CommonErrorCode.INVALID_PARAMETER;
        } else {
            errorCode = CommonErrorCode.INTERNAL_SERVER_ERROR;
        }

        return handleException(errorCode);
    }


    /* 예외처리 내부 메서드 */
    private ResponseEntity<Object> handleException(ErrorCode errorCode) {
        return ResponseEntity.status(errorCode.getHttpStatus())
                .body(makeErrorResponse(errorCode));

    }

    private ErrorResponse makeErrorResponse(ErrorCode errorCode) {
        return ErrorResponse.builder()
                .code(errorCode.name())
                .message(errorCode.getMessage())
                .build();
    }
}
