package com.trinity.ctc.domain.notification.sender;

import com.google.api.core.ApiFuture;
import com.google.firebase.messaging.*;
import com.trinity.ctc.domain.notification.dto.FcmSendingResultDto;
import com.trinity.ctc.domain.notification.dto.RetryDto;
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
                            if (sendResponse.getException().getMessagingErrorCode().equals(MessagingErrorCode.UNAVAILABLE)) {

                                return retrySendingProcess(new RetryDto(message.get(i).getToken(), message.get(i).getData()));
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
                            if (sendResponse.getException().getMessagingErrorCode().equals(MessagingErrorCode.UNAVAILABLE)) {
                                return retrySendingProcess(new RetryDto(message.getTokens().get(i), message.getData()));
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

    public FcmSendingResultDto retrySendingProcess(RetryDto retryDto) {

        Message message = Message.builder()
                .putData("title", retryDto.getData().get("title"))
                .putData("body", retryDto.getData().get("body"))
                .putData("url", retryDto.getData().get("url"))
                .setToken(retryDto.getToken())
                .build();

        FcmSendingResultDto sendingResult = null;
        try {
            sendingResult = retrySendingMessage(message, 0).get();
        } catch (Exception e) {
            e.getCause().printStackTrace();
        }
        return sendingResult;
    }

    @Async("retryThreadPool")
    public CompletableFuture<FcmSendingResultDto> retrySendingMessage(Message message, int retryCount) {
        ApiFuture<String> result = FirebaseMessaging.getInstance().sendAsync(message);
        try {
            result.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // 인터럽트 상태 복원
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof FirebaseMessagingException) {
                FirebaseMessagingException fcmException = (FirebaseMessagingException) cause;
                if (fcmException.getErrorCode().equals(MessagingErrorCode.UNAVAILABLE)) {
                    if (retryCount >= 3)
                        return CompletableFuture.completedFuture(new FcmSendingResultDto(LocalDateTime.now(), SentResult.FAILED, fcmException.getMessagingErrorCode()));
                    return retrySendingMessage(message, retryCount + 1);
                } else {
                    return CompletableFuture.completedFuture(new FcmSendingResultDto(LocalDateTime.now(), SentResult.FAILED, fcmException.getMessagingErrorCode()));
                }
            } else {
                // 그 외의 예외 처리
            }
        }
        return CompletableFuture.completedFuture(new FcmSendingResultDto(LocalDateTime.now(), SentResult.SUCCESS));
    }
}
