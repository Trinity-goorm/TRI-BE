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

import static com.trinity.ctc.domain.notification.formatter.NotificationMessageFormatter.createMessageWithUrl;

// 발송/응답 처리를 담당 -> Firebase SDK 메서드로 FCM 서버와 통신하는 class
@Slf4j
@Component
public class NotificationSender {

    // 재발송 최대 시도 횟수
    private static final int MAX_RETRY_COUNT = 3;
    // 재발송 지수 백오프
    private static final int[] EXPONENTIAL_BACKOFF = new int[]{10000, 1000, 2000, 4000};
    //  QUOTA_EXCEEDED 에 대한 재발송 최초 딜레이
    private static final int INITIAL_DELAY = 60000;


    /**
     * 단 건 알림 발송 메서드
     * @param message 발송할 메세지 정보를 담은 wrapper 객체
     * @return 전송 결과 DTO
     */
    public CompletableFuture<FcmSendingResultDto> sendSingleNotification(FcmMessage message) {
        // FCM 서버에 메세지 전송 -> 응답을 Future 객체로 반환
        ApiFuture<String> sendResponse = FirebaseMessaging.getInstance().sendAsync(message.getMessage());

        // 응답에 대한 처리 메서드 호출 -> 전송 결과 DTO 반환(비동기-none blocking 처리)
        return CompletableFuture.supplyAsync(() -> handleSingleResponse(sendResponse, message))
                .thenCompose(Function.identity());
    }

    /**
     * 발송된 단 건 알림에 대한 응답 처리 메서드
     * @param sendResponse 전송에 대한 응답(Future 객체)
     * @param message 전송된 메세지 정보를 담은 wrapper 객체
     * @return 전송 결과 DTO
     */
    @Async("response-handler")
    public CompletableFuture<FcmSendingResultDto> handleSingleResponse(ApiFuture<String> sendResponse, FcmMessage message) {
        try {
            // 전송 응답을 get 으로 반환
            sendResponse.get();
        } catch (Exception e) {
            // Fcm Exception 발생 시, 재전송 여부 판단을 위한 handleFcmException 호출
            if (e.getCause() instanceof FirebaseMessagingException fcmException) {
                return handleFcmException(fcmException, message, 0);
            } else {
                // 이외의 Exception 에 대해서 전송 실패 요청 에러
                throw new CustomException(FcmErrorCode.SENDING_REQUEST_FAILED);
            }
        }
        // 전송 응답을 받았다는 건 전송이 성공했다는 것
        // 전송 성공 응답 반환
        return CompletableFuture.completedFuture(new FcmSendingResultDto(LocalDateTime.now(), SentResult.SUCCESS));
    }


    /**
     * 여러 건의 알림 발송 메서드
     * @param messageList 발송할 메세지 정보를 담은 wrapper 객체의 리스트
     * @return 전송 결과 DTO 리스트
     */
    public CompletableFuture<List<FcmSendingResultDto>> sendEachNotification(List<FcmMessage> messageList) {
        // wrapper 객체 리스트 내에서 발송할 실제 Message 객체 get
        List<Message> messages = messageList.stream().map(FcmMessage::getMessage).toList();

        // FCM 서버에 메세지 전송 -> 응답을 Future 객체로 반환
        ApiFuture<BatchResponse> sendResponseFuture = FirebaseMessaging.getInstance().sendEachAsync(messages);

        // 응답에 대한 처리 메서드 호출 -> 전송 결과 DTO 리스트 반환(비동기-none blocking 처리)
        return CompletableFuture.supplyAsync(() -> handleEachResponse(sendResponseFuture, messageList))
                .thenCompose(Function.identity());
    }

    /**
     * 발송된 여러 건의 알림에 대한 응답 처리 메서드
     * @param batchResponse 전송에 대한 응답(Future 객체)
     * @param messageList 전송된 메세지 정보를 담은 wrapper 객체 리스트
     * @return 전송 결과 DTO 리스트
     */
    @Async("response-handler")
    public CompletableFuture<List<FcmSendingResultDto>> handleEachResponse(ApiFuture<BatchResponse> batchResponse, List<FcmMessage> messageList) {
        try {
            // 전송 응답을 get 으로 반환하고 응답 객체 내부의 개별 건에 대한 응답 리스트 get
            List<SendResponse> responses = batchResponse.get().getResponses();
            // 응답 리스트 내에서 전송 결과에 맞는 전송 결과 DTO를 반환 후, 리스트로 변환하여 전송 결과 DTO 리스트 반환
            List<FcmSendingResultDto> results = IntStream.range(0, responses.size())
                    .mapToObj(i -> {
                        // 개별 전송 건에 대한 응답 get
                        SendResponse sendResponse = responses.get(i);
                        // 전송 결과가 성공이면 전송 결과 DTO에 성공 데이터를 반환
                        if (sendResponse.isSuccessful()) {
                            return new FcmSendingResultDto(LocalDateTime.now(), SentResult.SUCCESS);
                        } else {
                            // 전송 결과가 실패일 경우, 재전송을 위한 메세지를 생성
                            FcmMessage retryMessage = createMessageWithUrl(
                                    messageList.get(i).getData().get("title"),
                                    messageList.get(i).getData().get("body"),
                                    messageList.get(i).getData().get("url"),
                                    messageList.get(i).getFcm()
                            );
                            // FcmException 에 따라 재전송/실패 처리를 판단하는 handleFcmException 메서드 호출 -> 결과에 따른 전송 결과 DTO 반환
                            return handleFcmException(sendResponse.getException(), retryMessage, 0).join();
                        }
                    })
                    .collect(Collectors.toList());
            // 전송 결과 DTO 리스트 반환
            return CompletableFuture.completedFuture(results);
        } catch (Exception e) {
            // 이외의 Exception 에 대해서 전송 실패 요청 에러
            throw new CustomException(FcmErrorCode.SENDING_REQUEST_FAILED);
        }
    }

    /**
     * MulticastMessage 발송 메서드 -> 같은 알림을 여러 명의 user 에게 보낼 경우
     * @param message MulticastMessage 의 정보를 담은 wrapper 객체
     * @return 전송 결과 DTO 리스트
     */
    public CompletableFuture<List<FcmSendingResultDto>> sendMulticastNotification(FcmMulticastMessage message) {
        // FCM 서버에 메세지 전송 -> 응답을 Future 객체로 반환
        ApiFuture<BatchResponse> sendResponseFuture = FirebaseMessaging.getInstance().sendEachForMulticastAsync(message.getMulticastMessage());

        // 응답에 대한 처리 메서드 호출 -> 전송 결과 DTO 리스트 반환(비동기-none blocking 처리)
        return CompletableFuture.supplyAsync(() -> handleMulticastResponse(sendResponseFuture, message))
                .thenCompose(Function.identity());
    }

    /**
     * 발송된 MulticastMessage 에 대한 응답 처리 메서드
     * @param batchResponse 전송에 대한 응답(Future 객체)
     * @param message 전송된 MulticastMessage 정보를 담은 wrapper 객체
     * @return 전송 결과 DTO 리스트
     */
    @Async("response-handler")
    public CompletableFuture<List<FcmSendingResultDto>> handleMulticastResponse(ApiFuture<BatchResponse> batchResponse, FcmMulticastMessage message) {
        try {
            // 전송 응답을 get 으로 반환하고 응답 객체 내부의 개별 건에 대한 응답 리스트 get
            List<SendResponse> responses = batchResponse.get().getResponses();
            // 응답 리스트 내에서 전송 결과에 맞는 전송 결과 DTO를 반환 후, 리스트로 변환하여 전송 결과 DTO 리스트 반환
            List<FcmSendingResultDto> results = IntStream.range(0, responses.size())
                    .mapToObj(i -> {
                        // 개별 전송 건에 대한 응답 get
                        SendResponse sendResponse = responses.get(i);
                        // 전송 결과가 성공이면 전송 결과 DTO에 성공 데이터를 반환
                        if (sendResponse.isSuccessful()) {
                            return new FcmSendingResultDto(LocalDateTime.now(), SentResult.SUCCESS);
                        } else {
                            // 전송 결과가 실패일 경우, 재전송을 위한 메세지를 생성
                            FcmMessage retryMessage = createMessageWithUrl(
                                    message.getData().get("title"),
                                    message.getData().get("body"),
                                    message.getData().get("url"),
                                    message.getFcmList().get(i)
                            );
                            // FcmException 에 따라 재전송/실패 처리를 판단하는 handleFcmException 메서드 호출 -> 결과에 따른 전송 결과 DTO 반환
                            return handleFcmException(sendResponse.getException(), retryMessage, 0).join();
                        }
                    })
                    .collect(Collectors.toList());
            // 전송 결과 DTO 리스트 반환
            return CompletableFuture.completedFuture(results);
        } catch (Exception e) {
            // 이외의 Exception 에 대해서 전송 실패 요청 에러 처리
            throw new CustomException(FcmErrorCode.SENDING_REQUEST_FAILED);
        }
    }

    /**
     * FcmException 에 따라 재전송/실패 처리를 판단하는 메서드
     * @param e FcmException
     * @param message 재전송할 메세지
     * @param retryCount 재전송 횟수
     * @return 전송 결과 DTO
     */
    private CompletableFuture<FcmSendingResultDto> handleFcmException(FirebaseMessagingException e, FcmMessage message, int retryCount) {
        // Firebase Messaging Error 를 get
        MessagingErrorCode errorCode = e.getMessagingErrorCode();

        return switch (errorCode) {
            // 500, 503에 해당할 경우 재전송 메서드 호출
            case UNAVAILABLE, INTERNAL -> retrySendingMessage(message, retryCount);
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
     * @param message 재전송할 메세지 정보를 담은 wrapper 객체
     * @param retryCount 재전송 횟수
     * @return 전송 결과 DTO
     */
    @Async("immediate-retry")
    public CompletableFuture<FcmSendingResultDto> retrySendingMessage(FcmMessage message, int retryCount) {
        try {
            // 정해진 지수 백오프만큼 스레드 대기
            Thread.sleep(EXPONENTIAL_BACKOFF[retryCount]);
            // FCM 서버에 메세지 전송 -> 응답으로 반환된 Future 객체를 get
            FirebaseMessaging.getInstance().sendAsync(message.getMessage()).get();
        } catch (Exception e) {
            // Fcm Exception 발생
            if (e.getCause() instanceof FirebaseMessagingException fcmException) {
                // 재전송 횟수가 최대 횟수 이상일 경우, 전송 실패로 전송 결과 DTO 반환
                if (retryCount >= MAX_RETRY_COUNT) {
                    return CompletableFuture.completedFuture(new FcmSendingResultDto(LocalDateTime.now(), SentResult.FAILED, fcmException.getMessagingErrorCode()));
                }
                // FcmException 에 따라 재전송/실패 처리를 판단하는 handleFcmException 메서드 호출
                // 같은 exception 이라면 사실상 재귀 호출이 됨
                return handleFcmException(fcmException, message, retryCount);
            } else {
                // 이외의 Exception 에 대해서 전송 실패 요청 에러
                throw new CustomException(FcmErrorCode.SENDING_REQUEST_FAILED);
            }
        }
        // exception 없이 전송 성공 -> 전송 성공 응답 반환
        return CompletableFuture.completedFuture(new FcmSendingResultDto(LocalDateTime.now(), SentResult.SUCCESS));
    }

    /**
     * FCM 서버에서의 응답이 429 일 경우에 대한 1분 지연 후 알림 재전송 메서드
     * @param message 재전송할 메세지 정보를 담은 wrapper 객체
     * @return 전송 결과 DTO
     */
    @Async("delayed-retry")
    public CompletableFuture<FcmSendingResultDto> retrySendingMessageWithDelay(FcmMessage message) {
        try {
            // 정해진 정책에 따라 지연 시간 이후 재전송 시작
            Thread.sleep(INITIAL_DELAY);
            // FCM 서버에 메세지 전송 -> 응답으로 반환된 Future 객체를 get
            FirebaseMessaging.getInstance().sendAsync(message.getMessage()).get();
        } catch (Exception e) {
            // Fcm Exception 발생
            if (e.getCause() instanceof FirebaseMessagingException fcmException) {
                MessagingErrorCode errorCode = fcmException.getMessagingErrorCode();
                // 500, 503에 해당하는 에러 코드일 경우, 재전송 메서드 호출
                if (errorCode.equals(MessagingErrorCode.UNAVAILABLE) || errorCode.equals(MessagingErrorCode.INTERNAL)) {
                    return retrySendingMessage(message, 0);
                } else {
                    // 그 외의 경우, 전송 실패로 전송 결과 DTO 반환
                    // 429가 다시 반환되었을 경우에도 실패 처리
                    return CompletableFuture.completedFuture(new FcmSendingResultDto(LocalDateTime.now(), SentResult.FAILED, fcmException.getMessagingErrorCode()));
                }
            } else {
                // 이외의 Exception 에 대해서 전송 실패 요청 에러
                throw new CustomException(FcmErrorCode.SENDING_REQUEST_FAILED);
            }
        }
        // exception 없이 전송 성공 -> 전송 성공 응답 반환
        return CompletableFuture.completedFuture(new FcmSendingResultDto(LocalDateTime.now(), SentResult.SUCCESS));
    }
}
