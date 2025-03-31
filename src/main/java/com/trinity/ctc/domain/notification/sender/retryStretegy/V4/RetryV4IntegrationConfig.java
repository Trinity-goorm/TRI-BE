package com.trinity.ctc.domain.notification.sender.retryStretegy.V4;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.MessagingErrorCode;
import com.trinity.ctc.domain.notification.dto.FcmSendingResultDto;
import com.trinity.ctc.domain.notification.result.SentResult;
import com.trinity.ctc.domain.notification.service.NotificationHistoryService;
import com.trinity.ctc.global.exception.CustomException;
import com.trinity.ctc.global.exception.error_code.FcmErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.scheduling.PollerMetadata;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scheduling.support.PeriodicTrigger;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.Executor;

import static com.trinity.ctc.domain.notification.formatter.NotificationHistoryFormatter.formattingSingleNotificationHistory;

@Slf4j
@Configuration
@EnableIntegration
@IntegrationComponentScan
@RequiredArgsConstructor
public class RetryV4IntegrationConfig {

    private final NotificationHistoryService notificationHistoryService;

    @Bean(name = "retryInputChannel")
    public DirectChannel retryInputChannel() {
        return new DirectChannel();
    }

    @Bean(name = "retrySendingChannel")
    public QueueChannel retrySendingChannel() {
        return new QueueChannel();
    }

    @Bean(name = PollerMetadata.DEFAULT_POLLER)
    public PollerMetadata defaultPoller(@Qualifier("response-handler") Executor executor) {
        PollerMetadata poller = new PollerMetadata();
        poller.setTrigger(new PeriodicTrigger(Duration.ofMillis(500)));
        poller.setMaxMessagesPerPoll(10);
        poller.setTaskExecutor(executor);
        return poller;
    }

    @Bean
    public IntegrationFlow retryFlow() {
        return IntegrationFlow.from("retryInputChannel")
                .<RetryMessageV4, MessagingErrorCode>route(
                        RetryMessageV4::errorCode,
                        mapping -> mapping
                                .channelMapping(MessagingErrorCode.UNAVAILABLE, "retrySendingChannel")
                                .channelMapping(MessagingErrorCode.INTERNAL, "retrySendingChannel")
                                .channelMapping(MessagingErrorCode.QUOTA_EXCEEDED, "retrySendingChannel")
                )
                .get();
    }

    @Bean
    public IntegrationFlow RetrySendingFlow(@Qualifier("retrySendingChannel") MessageChannel retryChannel) {
        return IntegrationFlow.from(retryChannel)
            .handle(Message.class, (message, headers) -> {
                RetryMessageV4 payload = (RetryMessageV4) message.getPayload();
                int retryCount = payload.retryCount();

                try {
                    FirebaseMessaging.getInstance().sendAsync(payload.message().getMessage()).get();
                    ;
                    FcmSendingResultDto success = new FcmSendingResultDto(LocalDateTime.now(), SentResult.SUCCESS);
                    notificationHistoryService.saveSingleNotificationHistory(formattingSingleNotificationHistory(payload.message(), success));
                } catch (Exception e) {
                    if (e.getCause() instanceof FirebaseMessagingException fcmException) {
                        MessagingErrorCode errorCode = fcmException.getMessagingErrorCode();

                        FcmSendingResultDto fail = new FcmSendingResultDto(LocalDateTime.now(), SentResult.FAILED);
                        notificationHistoryService.saveSingleNotificationHistory(formattingSingleNotificationHistory(payload.message(), fail));

                        int nextRetryCount = retryCount - 1;
                        if (nextRetryCount <= 0) {
                            return null;
                        }

                        if (errorCode == MessagingErrorCode.UNAVAILABLE || errorCode == MessagingErrorCode.INTERNAL) {
                            RetryMessageV4 nextRetry = new RetryMessageV4(payload.message(), nextRetryCount, errorCode);
                            long delayMillis = (long) Math.pow(2, 3 - nextRetryCount) * 1000;
                            return MessageBuilder.withPayload(nextRetry)
                                    .setHeader("delay", delayMillis)
                                    .build();
                        }
                    } else {
                        // TODO: 예외 처리 로직 개선 필요 -> FirebaseMessagingException 이외의 에러 핸들링
                        log.error("❌ 처리되지 않은 에러: ", e);
                        // 현재는 FirebaseMessagingException 이외의 Exception 에 대해서 일괄 전송 실패 요청 에러
                        throw new CustomException(FcmErrorCode.SENDING_REQUEST_FAILED);
                    }
                }
                return null;
            })
            .bridge()
            .get();
    }
}
