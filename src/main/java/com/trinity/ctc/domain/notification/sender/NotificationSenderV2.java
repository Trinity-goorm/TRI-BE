package com.trinity.ctc.domain.notification.sender;

import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutureCallback;
import com.google.api.core.ApiFutures;
import com.google.firebase.messaging.*;
import com.trinity.ctc.domain.notification.dto.FcmSendingResultDto;
import com.trinity.ctc.domain.notification.message.FcmMessage;
import com.trinity.ctc.domain.notification.message.FcmMulticastMessage;
import com.trinity.ctc.domain.notification.result.SentResult;
import com.trinity.ctc.domain.notification.sender.retryStretegy.NotificationRetryStrategy;
import com.trinity.ctc.global.exception.CustomException;
import com.trinity.ctc.global.exception.error_code.FcmErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.trinity.ctc.domain.notification.formatter.NotificationMessageFormatter.createMessageWithUrl;

// 발송/응답 처리를 담당 -> Firebase SDK 메서드로 FCM 서버와 통신하는 class
@Slf4j
@Component
public class NotificationSenderV2 implements NotificationSender {

    @Qualifier("response-handler")
    private Executor responseHandlerExecutor;

    private final Map<Integer, NotificationRetryStrategy> retryStrategies;
    private final int STRATEGY_VERSION = 1;


    public NotificationSenderV2(List<NotificationRetryStrategy> strategies) {
        this.retryStrategies = strategies.stream()
                .collect(Collectors.toMap(NotificationRetryStrategy::getStrategyVersion, Function.identity()));
    }

    /**
     * 단 건 알림 발송 메서드 및 응답 처리 메서드
     *
     * @param message 발송할 메세지 정보를 담은 wrapper 객체
     * @return 전송 결과 DTO
     */
    public CompletableFuture<FcmSendingResultDto> sendSingleNotification(FcmMessage message) {
        // FCM 서버에 메세지 전송 -> 응답을 Future 객체로 반환
        ApiFuture<String> sendResponse = FirebaseMessaging.getInstance().sendAsync(message.getMessage());

        // 응답에 대한 처리 메서드 호출 -> 전송 결과 DTO 반환(비동기-none blocking 처리)
        return handleSingleResponse(sendResponse, message);
    }

    /**
     * 발송된 단 건 알림에 대한 응답 처리 메서드
     *
     * @param sendResponse 전송에 대한 응답(Future 객체)
     * @param message      전송된 메세지 정보를 담은 wrapper 객체
     * @return 전송 결과 DTO
     */
    public CompletableFuture<FcmSendingResultDto> handleSingleResponse(ApiFuture<String> sendResponse, FcmMessage message) {
        CompletableFuture<FcmSendingResultDto> futureResult = new CompletableFuture<>();

        ApiFutures.addCallback(sendResponse, new ApiFutureCallback<>() {
            @Override
            public void onSuccess(String result) {
                futureResult.complete(new FcmSendingResultDto(LocalDateTime.now(), SentResult.SUCCESS));
            }

            @Override
            public void onFailure(Throwable t) {
                if (t instanceof FirebaseMessagingException fcmException) {
                    NotificationRetryStrategy strategy = retryStrategies.get(STRATEGY_VERSION);
                    strategy.retry(fcmException, message).thenAccept(futureResult::complete);
                } else {
                    // TODO: 예외 처리 로직 개선 필요 -> FirebaseMessagingException 이외의 에러 핸들링
                    log.error("❌ 처리되지 않은 에러: ", t);
                    // 현재는 FirebaseMessagingException 이외의 Exception 에 대해서 일괄 전송 실패 요청 에러
                    throw new CustomException(FcmErrorCode.SENDING_REQUEST_FAILED);
                }
            }
        }, responseHandlerExecutor);

        return futureResult;
    }

    /**
     * 여러 건의 알림 발송 메서드
     *
     * @param messageList 발송할 메세지 정보를 담은 wrapper 객체의 리스트
     * @return 전송 결과 DTO 리스트
     */
    public CompletableFuture<List<FcmSendingResultDto>> sendEachNotification(List<FcmMessage> messageList) {
        // wrapper 객체 리스트 내에서 발송할 실제 Message 객체 get
        List<Message> messages = messageList.stream().map(FcmMessage::getMessage).toList();

        // FCM 서버에 메세지 전송 -> 응답을 Future 객체로 반환
        ApiFuture<BatchResponse> sendResponseFuture = FirebaseMessaging.getInstance().sendEachAsync(messages);

        // 응답에 대한 처리 메서드 호출 -> 전송 결과 DTO 리스트 반환(비동기-none blocking 처리)
        return handleEachResponse(sendResponseFuture, messageList);
    }

    /**
     * 발송된 여러 건의 알림에 대한 응답 처리 메서드
     *
     * @param batchResponse 전송에 대한 응답(Future 객체)
     * @param messageList   전송된 메세지 정보를 담은 wrapper 객체 리스트
     * @return 전송 결과 DTO 리스트
     */
    public CompletableFuture<List<FcmSendingResultDto>> handleEachResponse(ApiFuture<BatchResponse> batchResponse, List<FcmMessage> messageList) {
        CompletableFuture<List<FcmSendingResultDto>> resultFuture = new CompletableFuture<>();

        ApiFutures.addCallback(batchResponse, new ApiFutureCallback<>() {
            @Override
            public void onSuccess(BatchResponse batchResponse) {
                NotificationRetryStrategy strategy = retryStrategies.get(STRATEGY_VERSION);
                List<SendResponse> responses = batchResponse.getResponses();

                List<CompletableFuture<FcmSendingResultDto>> futures = IntStream.range(0, responses.size())
                        .mapToObj(i -> {
                            SendResponse sendResponse = responses.get(i);
                            if (sendResponse.isSuccessful()) {
                                return CompletableFuture.completedFuture(new FcmSendingResultDto(LocalDateTime.now(), SentResult.SUCCESS));
                            } else {
                                return strategy.retry(sendResponse.getException(), messageList.get(i));
                            }
                        })
                        .toList();

                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                        .thenAccept(v -> resultFuture.complete(futures.stream().map(CompletableFuture::join).toList()));
            }

            @Override
            public void onFailure(Throwable t) {
                // TODO: 예외 처리 로직 개선 필요 -> FirebaseMessagingException 이외의 에러 핸들링
                log.error("❌ 처리되지 않은 에러: ", t);
                // 현재는 FirebaseMessagingException 이외의 Exception 에 대해서 일괄 전송 실패 요청 에러
                throw new CustomException(FcmErrorCode.SENDING_REQUEST_FAILED);
            }
        }, responseHandlerExecutor);

        return resultFuture;
    }

    /**
     * MulticastMessage 발송 메서드 -> 같은 알림을 여러 명의 user 에게 보낼 경우
     *
     * @param message MulticastMessage 의 정보를 담은 wrapper 객체
     * @return 전송 결과 DTO 리스트
     */
    public CompletableFuture<List<FcmSendingResultDto>> sendMulticastNotification(FcmMulticastMessage message) {
        // FCM 서버에 메세지 전송 -> 응답을 Future 객체로 반환
        ApiFuture<BatchResponse> sendResponseFuture = FirebaseMessaging.getInstance().sendEachForMulticastAsync(message.getMulticastMessage());

        // 응답에 대한 처리 메서드 호출 -> 전송 결과 DTO 리스트 반환(비동기-none blocking 처리)
        return handleMulticastResponse(sendResponseFuture, message);
    }

    /**
     * 발송된 MulticastMessage 에 대한 응답 처리 메서드
     *
     * @param batchResponse 전송에 대한 응답(Future 객체)
     * @param message       전송된 MulticastMessage 정보를 담은 wrapper 객체
     * @return 전송 결과 DTO 리스트
     */
    public CompletableFuture<List<FcmSendingResultDto>> handleMulticastResponse(ApiFuture<BatchResponse> batchResponse, FcmMulticastMessage message) {
        CompletableFuture<List<FcmSendingResultDto>> resultFuture = new CompletableFuture<>();

        ApiFutures.addCallback(batchResponse, new ApiFutureCallback<>() {
            @Override
            public void onSuccess(BatchResponse batchResponse) {
                NotificationRetryStrategy strategy = retryStrategies.get(STRATEGY_VERSION);
                List<SendResponse> responses = batchResponse.getResponses();

                List<CompletableFuture<FcmSendingResultDto>> futures = IntStream.range(0, responses.size())
                        .mapToObj(i -> {
                            SendResponse sendResponse = responses.get(i);
                            if (sendResponse.isSuccessful()) {
                                return CompletableFuture.completedFuture(new FcmSendingResultDto(LocalDateTime.now(), SentResult.SUCCESS));
                            } else {
                                FcmMessage retryMessage = createMessageWithUrl(
                                        message.getData().get("title"),
                                        message.getData().get("body"),
                                        message.getData().get("url"),
                                        message.getFcmList().get(i),
                                        message.getType()
                                );
                                return strategy.retry(sendResponse.getException(), retryMessage);
                            }
                        })
                        .toList();

                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                        .thenAccept(v -> resultFuture.complete(futures.stream().map(CompletableFuture::join).toList()));
            }

            @Override
            public void onFailure(Throwable t) {
                // TODO: 예외 처리 로직 개선 필요 -> FirebaseMessagingException 이외의 에러 핸들링
                log.error("❌ 처리되지 않은 에러: ", t);
                // 현재는 FirebaseMessagingException 이외의 Exception 에 대해서 일괄 전송 실패 요청 에러
                throw new CustomException(FcmErrorCode.SENDING_REQUEST_FAILED);
            }
        }, responseHandlerExecutor);

        return resultFuture;
    }

    @Override
    public int getStrategyVersion() {
        return 2;
    }
}
