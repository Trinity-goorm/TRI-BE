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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.trinity.ctc.domain.notification.fomatter.NotificationMessageUtil.createMessageWithUrl;

@Slf4j
@Component
public class NotificationSender {
    /**
     * 알림 전송 로직 중 FCM 메세지를 발송하는 내부 메서드
     *
     * @param messageList FCM 메세지 객체 리스트
     * @return
     */
    public List<FcmSendingResultDto> sendNotification(List<Message> messageList) {
        // FCM 메세지 전송 결과를 담는 DTO
        FcmSendingResultDto result;
        List<FcmSendingResultDto> resultList = new ArrayList<>();

        for (Message message : messageList) {
            try {
                // FCM 서버에 메세지 전송
                FirebaseMessaging.getInstance().send(message);
                // 전송 결과(전송 시간, 전송 상태)
                result = new FcmSendingResultDto(LocalDateTime.now(), SentResult.SUCCESS);
            } catch (FirebaseMessagingException e) {
                // 전송 결과(전송 시간, 전송 상태, 에러 코드)
                result = new FcmSendingResultDto(LocalDateTime.now(), SentResult.FAILED, e.getMessagingErrorCode());
            }
            resultList.add(result);
        }

        return resultList;
    }

    @Async
    public CompletableFuture<List<FcmSendingResultDto>> sendEachNotification(List<FcmMessage> messageList) {
        List<Message> messages = messageList.stream().map(FcmMessage::getMessage).toList();

        // FCM 서버에 메세지 전송
        ApiFuture<BatchResponse> sendResponseFuture = FirebaseMessaging.getInstance().sendEachAsync(messages, true);

        // 블로킹 없이 비동기 처리
        return CompletableFuture.supplyAsync(() -> handleResponse(sendResponseFuture, messageList))
                .thenCompose(Function.identity());
    }

    @Async("responseHandleThreadPool")
    public CompletableFuture<List<FcmSendingResultDto>> handleResponse(ApiFuture<BatchResponse> batchResponse, List<FcmMessage> message) {
        try {
            List<SendResponse> responses = batchResponse.get().getResponses();
            List<FcmSendingResultDto> results = IntStream.range(0, responses.size())
                    .mapToObj(i -> {
                        SendResponse sendResponse = responses.get(i);
                        if (sendResponse.isSuccessful()) {
                            return new FcmSendingResultDto(LocalDateTime.now(), SentResult.SUCCESS);
                        } else {
                            MessagingErrorCode errorCode = sendResponse.getException().getMessagingErrorCode();
                            if (errorCode.equals(MessagingErrorCode.UNAVAILABLE) || errorCode.equals(MessagingErrorCode.INTERNAL)) {
                                try {
                                    FcmMessage retryMessage = createMessageWithUrl(message.get(i).getData().get("title"),
                                            message.get(i).getData().get("body"),
                                            message.get(i).getData().get("url"),
                                            message.get(i).getToken());
                                    return retrySendingMessage(retryMessage, 0).get();
                                } catch (InterruptedException | ExecutionException e) {
                                    throw new RuntimeException(e);
                                }
                            } else {
                                return new FcmSendingResultDto(LocalDateTime.now(), SentResult.FAILED,
                                        sendResponse.getException().getMessagingErrorCode());
                            }
                        }
                    })
                    .collect(Collectors.toList());
            return CompletableFuture.completedFuture(results);
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new CustomException(FcmErrorCode.SENDING_REQUEST_FAILED);
        }
    }

    /**
     * MulticastMessage를 발송하는 내부 메서드
     *
     * @param message
     * @return
     */
    @Async("fixedThreadPoolExecutor")
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
                            MessagingErrorCode errorCode = sendResponse.getException().getMessagingErrorCode();
                            if (errorCode.equals(MessagingErrorCode.UNAVAILABLE) || errorCode.equals(MessagingErrorCode.INTERNAL)) {
                                try {
                                    FcmMessage retryMessage = createMessageWithUrl(message.getData().get("title"),
                                            message.getData().get("body"),
                                            message.getData().get("url"),
                                            message.getTokens().get(i));
                                    return retrySendingMessage(retryMessage, 0).get();
                                } catch (Exception e) {
                                    throw new CustomException(FcmErrorCode.SENDING_REQUEST_FAILED);
                                }
                            } else {
                                return new FcmSendingResultDto(LocalDateTime.now(), SentResult.FAILED,
                                        sendResponse.getException().getMessagingErrorCode());
                            }
                        }
                    })
                    .collect(Collectors.toList());
            return CompletableFuture.completedFuture(results);
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new CustomException(FcmErrorCode.SENDING_REQUEST_FAILED);
        }
    }

    private static final int MAX_RETRY_COUNT = 3;
    private static final int[] EXPONENTIAL_BACKOFF = new int[]{10000, 1000, 2000, 4000};
    private static final int INITIAL_DELAY = 60000;

    @Async("retryThreadPool")
    public CompletableFuture<FcmSendingResultDto> retrySendingMessage(FcmMessage message, int retryCount) {
        try {
            Thread.sleep(EXPONENTIAL_BACKOFF[retryCount]);
            FirebaseMessaging.getInstance().sendAsync(message.getMessage()).get();
        } catch (Exception e) {
            if (e.getCause() instanceof FirebaseMessagingException fcmException) {
                if (retryCount >= MAX_RETRY_COUNT)
                    return CompletableFuture.completedFuture(new FcmSendingResultDto(LocalDateTime.now(), SentResult.FAILED, fcmException.getMessagingErrorCode()));
                if (fcmException.getMessagingErrorCode().equals(MessagingErrorCode.UNAVAILABLE) || fcmException.getMessagingErrorCode().equals(MessagingErrorCode.INTERNAL)) {
                    return retrySendingMessage(message, retryCount + 1);
                } else if (fcmException.getMessagingErrorCode().equals(MessagingErrorCode.QUOTA_EXCEEDED)) {
                    return retryQuotaExceededException(message);
                } else {
                    return CompletableFuture.completedFuture(new FcmSendingResultDto(LocalDateTime.now(), SentResult.FAILED, fcmException.getMessagingErrorCode()));
                }
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
                if (fcmException.getMessagingErrorCode().equals(MessagingErrorCode.UNAVAILABLE) || fcmException.getMessagingErrorCode().equals(MessagingErrorCode.INTERNAL)) {
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
}
