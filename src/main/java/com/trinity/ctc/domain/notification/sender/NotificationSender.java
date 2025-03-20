package com.trinity.ctc.domain.notification.sender;

import com.google.api.core.ApiFuture;
import com.google.firebase.messaging.*;
import com.trinity.ctc.domain.notification.dto.FcmSendingResultDto;
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

    public List<FcmSendingResultDto> sendEachNotification(List<Message> messageList) {
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
                                return retryMulticastMessage(message, i, 0);
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

    @Async
    public FcmSendingResultDto retryMulticastMessage(FcmMulticastMessage multicastMessage, int index, int retryCount) {

        Message message = createMessageWithUrl(multicastMessage.getData().get("title"), multicastMessage.getData().get("body"),
                multicastMessage.getData().get("url"), multicastMessage.getTokens().get(index));

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
                        return new FcmSendingResultDto(LocalDateTime.now(), SentResult.FAILED, fcmException.getMessagingErrorCode());
                    return retryMulticastMessage(multicastMessage, index, retryCount + 1);
                } else {
                    return new FcmSendingResultDto(LocalDateTime.now(), SentResult.FAILED, fcmException.getMessagingErrorCode());
                }
            } else {
                // 그 외의 예외 처리
            }
        }
        return new FcmSendingResultDto(LocalDateTime.now(), SentResult.SUCCESS);
    }
}
