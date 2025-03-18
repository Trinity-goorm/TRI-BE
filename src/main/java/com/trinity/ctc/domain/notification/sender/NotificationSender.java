package com.trinity.ctc.domain.notification.sender;

import com.google.api.core.ApiFuture;
import com.google.firebase.messaging.*;
import com.trinity.ctc.domain.notification.dto.FcmSendingResultDto;
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

@Slf4j
@Component
public class NotificationSender {
    /**
     * 알림 전송 로직 중 FCM 메세지를 발송하는 내부 메서드
     *
     * @param messageList FCM 메세지 객체 리스트
     * @return
     */
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
    public CompletableFuture<List<FcmSendingResultDto>> sendMulticastNotification(MulticastMessage message, int batchCount, int clearCount) {
        // FCM 서버에 메세지 전송
        ApiFuture<BatchResponse> sendResponseFuture = FirebaseMessaging.getInstance().sendEachForMulticastAsync(message, true);

        log.info("Batch Count: {}", batchCount);
        if (batchCount == clearCount) log.info("전송완료!!!!!!!!!!!!!");

        // 블로킹 없이 비동기 처리
        return CompletableFuture.supplyAsync(() -> {
            try {
                BatchResponse batchResponse = sendResponseFuture.get();  // 결과를 가져옴
                return batchResponse.getResponses().stream()
                        .map(sendResponse -> new FcmSendingResultDto(
                                LocalDateTime.now(),
                                sendResponse.isSuccessful() ? SentResult.SUCCESS : SentResult.FAILED,
                                sendResponse.isSuccessful() ? null : sendResponse.getException().getMessagingErrorCode()
                        ))
                        .toList();
            } catch (InterruptedException | ExecutionException e) {
                Thread.currentThread().interrupt();
                throw new CustomException(FcmErrorCode.SENDING_REQUEST_FAILED);
            }
        });
    }

    /**
     * 재발송 로직 구현!!!
     */
}
