package com.trinity.ctc.domain.notification.sender;

import com.google.api.core.ApiFuture;
import com.google.firebase.messaging.*;
import com.trinity.ctc.domain.notification.dto.FcmSendingResultDto;
import com.trinity.ctc.domain.notification.message.FcmMessage;
import com.trinity.ctc.domain.notification.message.FcmMulticastMessage;
import com.trinity.ctc.domain.notification.result.SentResult;
import com.trinity.ctc.global.exception.CustomException;
import com.trinity.ctc.global.exception.error_code.FcmErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.trinity.ctc.domain.notification.fomatter.NotificationMessageUtil.createMessageWithUrl;

@Slf4j
@Component
public class NotificationSender {

    // 재발송 최대 시도 횟수
    private static final int MAX_RETRY_COUNT = 3;
    // 재발송 지수 백오프
    private static final int[] EXPONENTIAL_BACKOFF = new int[]{10000, 1000, 2000, 4000};
    //  QUOTA_EXCEEDED 에 대한 재발송 최초 딜레이
    private static final int INITIAL_DELAY = 60000;


    public CompletableFuture<FcmSendingResultDto> sendSingleNotification(FcmMessage message) {
        // FCM 서버에 메세지 전송
        ApiFuture<String> sendResponse = FirebaseMessaging.getInstance().sendAsync(message.getMessage(), true);

        // 블로킹 없이 비동기 처리
        return CompletableFuture.supplyAsync(() -> handleResponse(sendResponse, message))
                .thenCompose(Function.identity());
    }

    @Async
    public CompletableFuture<FcmSendingResultDto> handleResponse(ApiFuture<String> sendResponse, FcmMessage message) {
        try {
            sendResponse.get();

        } catch (Exception e) {
            if (e.getCause() instanceof FirebaseMessagingException fcmException) {
                return handleFcmException(fcmException, message, 0);
            } else {
                throw new CustomException(FcmErrorCode.SENDING_REQUEST_FAILED);
            }
        }
        return CompletableFuture.completedFuture(new FcmSendingResultDto(LocalDateTime.now(), SentResult.SUCCESS));
    }


    public CompletableFuture<List<FcmSendingResultDto>> sendEachNotification(List<FcmMessage> messageList) {
        List<Message> messages = messageList.stream().map(FcmMessage::getMessage).toList();

        // FCM 서버에 메세지 전송
        ApiFuture<BatchResponse> sendResponseFuture = FirebaseMessaging.getInstance().sendEachAsync(messages, true);

        // 블로킹 없이 비동기 처리
        return CompletableFuture.supplyAsync(() -> handleEachResponse(sendResponseFuture, messageList))
                .thenCompose(Function.identity());
    }

    @Async("responseHandleThreadPool")
    public CompletableFuture<List<FcmSendingResultDto>> handleEachResponse(ApiFuture<BatchResponse> batchResponse, List<FcmMessage> message) {
        try {
            List<SendResponse> responses = batchResponse.get().getResponses();
            List<FcmSendingResultDto> results = IntStream.range(0, responses.size())
                    .mapToObj(i -> {
                        SendResponse sendResponse = responses.get(i);
                        if (sendResponse.isSuccessful()) {
                            return new FcmSendingResultDto(LocalDateTime.now(), SentResult.SUCCESS);
                        } else {
                            FcmMessage retryMessage = createMessageWithUrl(
                                    message.get(i).getData().get("title"),
                                    message.get(i).getData().get("body"),
                                    message.get(i).getData().get("url"),
                                    message.get(i).getFcm()
                            );
                            return handleFcmException(sendResponse.getException(), retryMessage, 0).join();
                        }
                    })
                    .collect(Collectors.toList());
            return CompletableFuture.completedFuture(results);
        } catch (Exception e) {
            throw new CustomException(FcmErrorCode.SENDING_REQUEST_FAILED);
        }
    }

    /**
     * MulticastMessage를 발송하는 내부 메서드
     *
     * @param message
     * @return
     */
    public CompletableFuture<List<FcmSendingResultDto>> sendMulticastNotification(FcmMulticastMessage message) {
        // FCM 서버에 메세지 전송
        ApiFuture<BatchResponse> sendResponseFuture = FirebaseMessaging.getInstance().sendEachForMulticastAsync(message.getMulticastMessage(), true);

        // 블로킹 없이 비동기 처리
        return CompletableFuture.supplyAsync(() -> handleMulticastResponse(sendResponseFuture, message))
                .thenCompose(Function.identity());
    }

    @Async("responseHandleThreadPool")
    public CompletableFuture<List<FcmSendingResultDto>> handleMulticastResponse(ApiFuture<BatchResponse> batchResponse, FcmMulticastMessage message) {
        try {
            List<SendResponse> responses = batchResponse.get().getResponses();
            List<FcmSendingResultDto> results = IntStream.range(0, responses.size())
                    .mapToObj(i -> {
                        SendResponse sendResponse = responses.get(i);
                        if (sendResponse.isSuccessful()) {
                            return new FcmSendingResultDto(LocalDateTime.now(), SentResult.SUCCESS);
                        } else {
                            FcmMessage retryMessage = createMessageWithUrl(
                                    message.getData().get("title"),
                                    message.getData().get("body"),
                                    message.getData().get("url"),
                                    message.getFcmList().get(i)
                            );
                            return handleFcmException(sendResponse.getException(), retryMessage, 0).join();
                        }
                    })
                    .collect(Collectors.toList());
            return CompletableFuture.completedFuture(results);
        } catch (Exception e) {
            throw new CustomException(FcmErrorCode.SENDING_REQUEST_FAILED);
        }
    }

    @Async("retryThreadPool")
    public CompletableFuture<FcmSendingResultDto> retrySendingMessage(FcmMessage message, int retryCount) {
        try {
            Thread.sleep(EXPONENTIAL_BACKOFF[retryCount]);
            FirebaseMessaging.getInstance().sendAsync(message.getMessage()).get();
        } catch (Exception e) {
            if (e.getCause() instanceof FirebaseMessagingException fcmException) {
                if (retryCount >= MAX_RETRY_COUNT) {
                    return CompletableFuture.completedFuture(new FcmSendingResultDto(LocalDateTime.now(), SentResult.FAILED, fcmException.getMessagingErrorCode()));
                }
                return handleFcmException(fcmException, message, retryCount);
            } else {
                throw new CustomException(FcmErrorCode.SENDING_REQUEST_FAILED);
            }
        }
        return CompletableFuture.completedFuture(new FcmSendingResultDto(LocalDateTime.now(), SentResult.SUCCESS));
    }

    @Async("cashedThreadPool")
    public CompletableFuture<FcmSendingResultDto> retryQuotaExceededException(FcmMessage message) {
        try {
            Thread.sleep(INITIAL_DELAY);
            FirebaseMessaging.getInstance().sendAsync(message.getMessage()).get();
        } catch (Exception e) {
            if (e.getCause() instanceof FirebaseMessagingException fcmException) {
                MessagingErrorCode errorCode = fcmException.getMessagingErrorCode();
                if (errorCode.equals(MessagingErrorCode.UNAVAILABLE) || errorCode.equals(MessagingErrorCode.INTERNAL)) {
                    return retrySendingMessage(message, 0);
                } else {
                    return CompletableFuture.completedFuture(new FcmSendingResultDto(LocalDateTime.now(), SentResult.FAILED, fcmException.getMessagingErrorCode()));
                }
            } else {
                throw new CustomException(FcmErrorCode.SENDING_REQUEST_FAILED);
            }
        }
        return CompletableFuture.completedFuture(new FcmSendingResultDto(LocalDateTime.now(), SentResult.SUCCESS));
    }

    private CompletableFuture<FcmSendingResultDto> handleFcmException(FirebaseMessagingException e, FcmMessage message, int retryCount) {
        MessagingErrorCode errorCode = e.getMessagingErrorCode();

        return switch (errorCode) {
            case UNAVAILABLE, INTERNAL -> retrySendingMessage(message, retryCount);
            case QUOTA_EXCEEDED -> retryQuotaExceededException(message);
            default -> CompletableFuture.completedFuture(
                    new FcmSendingResultDto(LocalDateTime.now(), SentResult.FAILED, errorCode)
            );
        };
    }
}
