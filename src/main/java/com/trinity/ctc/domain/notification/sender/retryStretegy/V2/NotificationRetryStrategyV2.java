package com.trinity.ctc.domain.notification.sender.retryStretegy.V2;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.MessagingErrorCode;
import com.trinity.ctc.domain.notification.dto.FcmSendingResultDto;
import com.trinity.ctc.domain.notification.message.FcmMessage;
import com.trinity.ctc.domain.notification.result.SentResult;
import com.trinity.ctc.domain.notification.sender.retryStretegy.NotificationRetryStrategy;
import com.trinity.ctc.global.exception.CustomException;
import com.trinity.ctc.global.exception.error_code.FcmErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
public class NotificationRetryStrategyV2 implements NotificationRetryStrategy {

    // 재발송 지수 백오프
    private static final int[] EXPONENTIAL_BACKOFF = {10000, 4000, 2000, 1000};
    // QUOTA_EXCEEDED 에 대한 재발송 최초 딜레이
    private static final int ONE_MINUTE_DELAY = 60000;

    /**
     * FcmException 에 따라 재전송/실패 처리를 판단하는 메서드
     *
     * @param e       FcmException
     * @param message 재전송할 메세지
     * @return 전송 결과 DTO
     */
    @Override
    public CompletableFuture<FcmSendingResultDto> retry(FirebaseMessagingException e, FcmMessage message) {
        // Firebase Messaging Error 를 get
        MessagingErrorCode errorCode = e.getMessagingErrorCode();

        return switch (errorCode) {
            // 500, 503에 해당할 경우 재전송 메서드 호출
            case UNAVAILABLE, INTERNAL -> retrySendingMessage(message, 3);
            // 429에 해당할 경우 1분 후 재전송하는 메서드 호출
            case QUOTA_EXCEEDED -> retrySendingMessageWithDelay(message);
            // 그 외의 경우 전송 실패로 전송 결과 DTO 반환
            default -> CompletableFuture.completedFuture(
                    new FcmSendingResultDto(LocalDateTime.now(), SentResult.FAILED, errorCode)
            );
        };
    }

    /**
     * FCM 서버에서의 응답이 500, 503 일 경우에 대한 알림 재전송 메서드
     *
     * @param message    재전송할 메세지 정보를 담은 wrapper 객체
     * @param retryCount 재전송 횟수
     * @return 전송 결과 DTO
     */
    @Async("retry-thread")
    public CompletableFuture<FcmSendingResultDto> retrySendingMessage(FcmMessage message, int retryCount) {
        try {
            // 정해진 지수 백오프만큼 스레드 대기
            Thread.sleep(EXPONENTIAL_BACKOFF[0] + EXPONENTIAL_BACKOFF[retryCount]);
            // FCM 서버에 메세지 전송 -> 응답으로 반환된 Future 객체를 get
            FirebaseMessaging.getInstance().sendAsync(message.getMessage()).get();
        } catch (Exception e) {

            // Fcm Exception 발생
            if (e.getCause() instanceof FirebaseMessagingException fcmException) {
                MessagingErrorCode errorCode = fcmException.getMessagingErrorCode();
                retryCount--;
                if (retryCount <= 0) CompletableFuture.completedFuture(new FcmSendingResultDto(LocalDateTime.now(),
                        SentResult.FAILED,
                        fcmException.getMessagingErrorCode())
                );

                // 500, 503에 해당하는 에러 코드일 경우, 재전송 메서드 호출
                if (errorCode.equals(MessagingErrorCode.UNAVAILABLE) || errorCode.equals(MessagingErrorCode.INTERNAL)) {
                    return retrySendingMessage(message, retryCount);
                    // 429에 해당하는 에러 코드의 경우, 재전송 메서드 호출
                } else if (errorCode.equals(MessagingErrorCode.QUOTA_EXCEEDED)) {
                    return retrySendingMessageWithDelay(message);
                    // 그 이외의 에러 코드의 경우, 실패 결과 반환
                } else {
                    return CompletableFuture.completedFuture(new FcmSendingResultDto(LocalDateTime.now(),
                            SentResult.FAILED,
                            fcmException.getMessagingErrorCode())
                    );
                }
                // FcmException 에 따라 재전송/실패 처리를 판단하는 handleFcmException 메서드 호출
                // 같은 exception 이라면 사실상 재귀 호출이 됨
            } else {
                // TODO: 예외 처리 로직 개선 필요 -> FirebaseMessagingException 이외의 에러 핸들링
                log.error("❌ 처리되지 않은 에러: ", e);
                // 현재는 FirebaseMessagingException 이외의 Exception 에 대해서 일괄 전송 실패 요청 에러
                throw new CustomException(FcmErrorCode.SENDING_REQUEST_FAILED);
            }
        }
        // exception 없이 전송 성공 -> 전송 성공 응답 반환
        return CompletableFuture.completedFuture(new FcmSendingResultDto(LocalDateTime.now(), SentResult.SUCCESS));
    }

    /**
     * FCM 서버에서의 응답이 429 일 경우에 대한 1분 지연 후 알림 재전송 메서드
     *
     * @param message 재전송할 메세지 정보를 담은 wrapper 객체
     * @return 전송 결과 DTO
     */
    @Async("retry-thread")
    public CompletableFuture<FcmSendingResultDto> retrySendingMessageWithDelay(FcmMessage message) {
        try {
            // 정해진 정책에 따라 지연 시간 이후 재전송 시작
            Thread.sleep(ONE_MINUTE_DELAY);
            // FCM 서버에 메세지 전송 -> 응답으로 반환된 Future 객체를 get
            FirebaseMessaging.getInstance().sendAsync(message.getMessage()).get();
        } catch (Exception e) {
            // Fcm Exception 발생
            if (e.getCause() instanceof FirebaseMessagingException fcmException) {
                MessagingErrorCode errorCode = fcmException.getMessagingErrorCode();
                // 500, 503에 해당하는 에러 코드일 경우, 재전송 메서드 호출
                if (errorCode.equals(MessagingErrorCode.UNAVAILABLE) || errorCode.equals(MessagingErrorCode.INTERNAL)) {
                    return retrySendingMessage(message, 3);
                } else {
                    // 그 외의 경우, 전송 실패로 전송 결과 DTO 반환
                    // 429가 다시 반환되었을 경우에도 실패 처리
                    return CompletableFuture.completedFuture(new FcmSendingResultDto(LocalDateTime.now(),
                            SentResult.FAILED,
                            fcmException.getMessagingErrorCode())
                    );
                }
            } else {
                // TODO: 예외 처리 로직 개선 필요 -> FirebaseMessagingException 이외의 에러 핸들링
                log.error("❌ 처리되지 않은 에러: ", e);
                // 현재는 FirebaseMessagingException 이외의 Exception 에 대해서 일괄 전송 실패 요청 에러
                throw new CustomException(FcmErrorCode.SENDING_REQUEST_FAILED);
            }
        }
        // exception 없이 전송 성공 -> 전송 성공 응답 반환
        return CompletableFuture.completedFuture(new FcmSendingResultDto(LocalDateTime.now(), SentResult.SUCCESS));
    }

    @Override
    public int getStrategyVersion() {
        return 2;
    }
}
